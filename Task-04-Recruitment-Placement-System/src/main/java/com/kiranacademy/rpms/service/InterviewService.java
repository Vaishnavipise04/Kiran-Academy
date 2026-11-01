package com.kiranacademy.rpms.service;

import com.kiranacademy.rpms.dao.ApplicationDAO;
import com.kiranacademy.rpms.dao.AuditDAO;
import com.kiranacademy.rpms.dao.FeedbackDAO;
import com.kiranacademy.rpms.dao.InterviewDAO;
import com.kiranacademy.rpms.exception.EntityNotFoundException;
import com.kiranacademy.rpms.exception.InvalidStateTransitionException;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.exception.ValidationException;
import com.kiranacademy.rpms.model.Application;
import com.kiranacademy.rpms.model.ApplicationStatus;
import com.kiranacademy.rpms.model.AuditLog;
import com.kiranacademy.rpms.model.Interview;
import com.kiranacademy.rpms.model.InterviewFeedback;
import com.kiranacademy.rpms.model.InterviewMode;
import com.kiranacademy.rpms.model.InterviewResult;
import com.kiranacademy.rpms.model.InterviewStatus;
import com.kiranacademy.rpms.utility.Db;
import com.kiranacademy.rpms.utility.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class InterviewService {

    private static final Set<ApplicationStatus> SCHEDULABLE = Set.of(
            ApplicationStatus.SHORTLISTED,
            ApplicationStatus.INTERVIEWED,
            ApplicationStatus.ON_HOLD
    );

    private final InterviewDAO interviewDAO;
    private final FeedbackDAO feedbackDAO;
    private final ApplicationDAO applicationDAO;
    private final AuditDAO auditDAO;

    public InterviewService(InterviewDAO interviewDAO, FeedbackDAO feedbackDAO,
                            ApplicationDAO applicationDAO, AuditDAO auditDAO) {
        this.interviewDAO = interviewDAO;
        this.feedbackDAO = feedbackDAO;
        this.applicationDAO = applicationDAO;
        this.auditDAO = auditDAO;
    }

    public Interview schedule(int applicationId, String roundType, LocalDateTime scheduledAt,
                              String interviewer, InterviewMode mode, String performedBy)
            throws RPMSException {
        if (roundType == null || roundType.isBlank()) {
            throw new ValidationException("Round type is required");
        }
        if (interviewer == null || interviewer.isBlank()) {
            throw new ValidationException("Interviewer name is required");
        }
        if (scheduledAt == null || scheduledAt.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Interview must be scheduled in the future");
        }
        return Db.transaction(connection -> {
            Application application = applicationDAO.findById(connection, applicationId)
                    .orElseThrow(() -> new EntityNotFoundException("Application", applicationId));
            if (!SCHEDULABLE.contains(application.status())) {
                throw new InvalidStateTransitionException("Cannot schedule an interview for an "
                        + "application in status " + application.status()
                        + ". Shortlist the application first.");
            }
            if (!application.status().canTransitionTo(ApplicationStatus.INTERVIEW_SCHEDULED)) {
                throw new InvalidStateTransitionException("Application cannot move from "
                        + application.status() + " to INTERVIEW_SCHEDULED");
            }

            int roundNo = interviewDAO.nextRoundNo(connection, applicationId);
            Interview interview = new Interview(0, applicationId, roundNo, roundType.trim(),
                    scheduledAt, interviewer.trim(), mode, InterviewStatus.SCHEDULED,
                    LocalDateTime.now(), application.candidateName(), application.jobTitle(),
                    application.companyName());
            int id = interviewDAO.insert(connection, interview);

            applicationDAO.updateStatus(connection, applicationId, ApplicationStatus.INTERVIEW_SCHEDULED);
            auditDAO.insert(connection, AuditLog.of("APPLICATION", applicationId,
                    "INTERVIEW_SCHEDULED", application.status().name(),
                    ApplicationStatus.INTERVIEW_SCHEDULED.name(), performedBy,
                    "Round " + roundNo + " scheduled with " + interviewer.trim()));
            auditDAO.insert(connection, AuditLog.of("INTERVIEW", id, "SCHEDULED", null,
                    InterviewStatus.SCHEDULED.name(), performedBy, roundType.trim()));

            return interviewDAO.findById(connection, id)
                    .orElseThrow(() -> new EntityNotFoundException("Interview", id));
        });
    }

    public InterviewFeedback submitFeedback(int interviewId, int technicalRating,
                                            int communicationRating, int attitudeRating,
                                            String comments, InterviewResult result,
                                            String performedBy) throws RPMSException {
        ValidationUtil.rating(technicalRating, "Technical rating");
        ValidationUtil.rating(communicationRating, "Communication rating");
        ValidationUtil.rating(attitudeRating, "Attitude rating");
        if (result == null) {
            throw new ValidationException("Interview result is required");
        }
        return Db.transaction(connection -> {
            Interview interview = interviewDAO.findById(connection, interviewId)
                    .orElseThrow(() -> new EntityNotFoundException("Interview", interviewId));
            if (interview.status() == InterviewStatus.CANCELLED
                    || interview.status() == InterviewStatus.NO_SHOW) {
                throw new InvalidStateTransitionException(
                        "Feedback cannot be recorded for a " + interview.status() + " interview");
            }
            if (feedbackDAO.existsByInterview(connection, interviewId)) {
                throw new ValidationException("Feedback already exists for this interview round");
            }

            InterviewFeedback feedback = new InterviewFeedback(0, interviewId, technicalRating,
                    communicationRating, attitudeRating, comments, result, performedBy,
                    LocalDateTime.now());
            int feedbackId = feedbackDAO.insert(connection, feedback);

            if (interview.status() == InterviewStatus.SCHEDULED) {
                interviewDAO.updateStatus(connection, interviewId, InterviewStatus.COMPLETED);
                auditDAO.insert(connection, AuditLog.of("INTERVIEW", interviewId,
                        "STATUS_CHANGED", InterviewStatus.SCHEDULED.name(),
                        InterviewStatus.COMPLETED.name(), performedBy, "Interview completed"));
            }

            Application application = applicationDAO.findById(connection, interview.applicationId())
                    .orElseThrow(() -> new EntityNotFoundException("Application",
                            interview.applicationId()));
            if (application.status() == ApplicationStatus.INTERVIEW_SCHEDULED) {
                applicationDAO.updateStatus(connection, application.id(), ApplicationStatus.INTERVIEWED);
                auditDAO.insert(connection, AuditLog.of("APPLICATION", application.id(),
                        "STATUS_CHANGED", ApplicationStatus.INTERVIEW_SCHEDULED.name(),
                        ApplicationStatus.INTERVIEWED.name(), performedBy,
                        "Feedback result: " + result));
            } else if (application.status() != ApplicationStatus.INTERVIEWED) {
                throw new InvalidStateTransitionException("Application is in status "
                        + application.status() + " and cannot accept interview feedback");
            }

            return feedbackDAO.findByInterview(connection, interviewId).orElse(feedback);
        });
    }

    public Interview cancel(int interviewId, String reason, String performedBy) throws RPMSException {
        return Db.transaction(connection -> {
            Interview interview = interviewDAO.findById(connection, interviewId)
                    .orElseThrow(() -> new EntityNotFoundException("Interview", interviewId));
            if (!interview.status().canTransitionTo(InterviewStatus.CANCELLED)) {
                throw new InvalidStateTransitionException(
                        "Interview cannot be cancelled from status " + interview.status());
            }
            interviewDAO.updateStatus(connection, interviewId, InterviewStatus.CANCELLED);

            Application application = applicationDAO.findById(connection, interview.applicationId())
                    .orElseThrow(() -> new EntityNotFoundException("Application",
                            interview.applicationId()));
            if (application.status() == ApplicationStatus.INTERVIEW_SCHEDULED
                    && application.status().canTransitionTo(ApplicationStatus.ON_HOLD)) {
                applicationDAO.updateStatus(connection, application.id(), ApplicationStatus.ON_HOLD);
                auditDAO.insert(connection, AuditLog.of("APPLICATION", application.id(),
                        "STATUS_CHANGED", ApplicationStatus.INTERVIEW_SCHEDULED.name(),
                        ApplicationStatus.ON_HOLD.name(), performedBy,
                        "Interview cancelled: " + safe(reason)));
            }
            auditDAO.insert(connection, AuditLog.of("INTERVIEW", interviewId, "CANCELLED",
                    InterviewStatus.SCHEDULED.name(), InterviewStatus.CANCELLED.name(),
                    performedBy, safe(reason)));
            return interviewDAO.findById(connection, interviewId)
                    .orElseThrow(() -> new EntityNotFoundException("Interview", interviewId));
        });
    }

    public Interview findById(int interviewId) throws RPMSException {
        return Db.read(connection -> interviewDAO.findById(connection, interviewId))
                .orElseThrow(() -> new EntityNotFoundException("Interview", interviewId));
    }

    public List<Interview> findAll() throws RPMSException {
        return Db.read(interviewDAO::findAll);
    }

    public List<Interview> findForApplication(int applicationId) throws RPMSException {
        return Db.read(connection -> interviewDAO.findByApplication(connection, applicationId));
    }

    public List<Interview> findForCandidate(int candidateId) throws RPMSException {
        return Db.read(connection -> interviewDAO.findByCandidate(connection, candidateId));
    }

    public List<Interview> findUpcoming() throws RPMSException {
        return Db.read(interviewDAO::findUpcoming);
    }

    public List<InterviewFeedback> findFeedbackForApplication(int applicationId) throws RPMSException {
        return Db.read(connection -> feedbackDAO.findByApplication(connection, applicationId));
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "No reason provided" : value.trim();
    }
}
