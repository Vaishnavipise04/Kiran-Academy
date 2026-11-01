package com.kiranacademy.rpms.service;

import com.kiranacademy.rpms.dao.ApplicationDAO;
import com.kiranacademy.rpms.dao.AuditDAO;
import com.kiranacademy.rpms.dao.CandidateDAO;
import com.kiranacademy.rpms.dao.JobDAO;
import com.kiranacademy.rpms.exception.DuplicateApplicationException;
import com.kiranacademy.rpms.exception.EligibilityException;
import com.kiranacademy.rpms.exception.EntityNotFoundException;
import com.kiranacademy.rpms.exception.InvalidStateTransitionException;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.exception.ValidationException;
import com.kiranacademy.rpms.model.Application;
import com.kiranacademy.rpms.model.ApplicationStatus;
import com.kiranacademy.rpms.model.AuditLog;
import com.kiranacademy.rpms.model.Candidate;
import com.kiranacademy.rpms.model.Job;
import com.kiranacademy.rpms.utility.AppLogger;
import com.kiranacademy.rpms.utility.Db;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class ApplicationService {

    private static final Set<ApplicationStatus> MANUAL_STAFF_STATUSES = Set.of(
            ApplicationStatus.SHORTLISTED,
            ApplicationStatus.SELECTED,
            ApplicationStatus.REJECTED,
            ApplicationStatus.ON_HOLD
    );

    private final ApplicationDAO applicationDAO;
    private final CandidateDAO candidateDAO;
    private final JobDAO jobDAO;
    private final AuditDAO auditDAO;
    private final EligibilityEngine eligibilityEngine;

    public ApplicationService(ApplicationDAO applicationDAO, CandidateDAO candidateDAO,
                              JobDAO jobDAO, AuditDAO auditDAO, EligibilityEngine eligibilityEngine) {
        this.applicationDAO = applicationDAO;
        this.candidateDAO = candidateDAO;
        this.jobDAO = jobDAO;
        this.auditDAO = auditDAO;
        this.eligibilityEngine = eligibilityEngine;
    }

    public EligibilityResult checkEligibility(int candidateId, int jobId) throws RPMSException {
        return Db.read(connection -> {
            Candidate candidate = candidateDAO.findById(connection, candidateId)
                    .orElseThrow(() -> new EntityNotFoundException("Candidate", candidateId));
            Job job = jobDAO.findById(connection, jobId)
                    .orElseThrow(() -> new EntityNotFoundException("Job", jobId));
            return eligibilityEngine.check(candidate, job);
        });
    }

    public Application apply(int candidateId, int jobId, String performedBy) throws RPMSException {
        return Db.transaction(connection -> {
            Candidate candidate = candidateDAO.findById(connection, candidateId)
                    .orElseThrow(() -> new EntityNotFoundException("Candidate", candidateId));
            Job job = jobDAO.findById(connection, jobId)
                    .orElseThrow(() -> new EntityNotFoundException("Job", jobId));

            java.util.Optional<Application> existing =
                    applicationDAO.findByCandidateAndJob(connection, candidateId, jobId);
            if (existing.isPresent()) {
                throw new DuplicateApplicationException(
                        "Candidate already applied to this job. Application #" + existing.get().id()
                                + " is currently " + existing.get().status());
            }

            EligibilityResult result = eligibilityEngine.check(candidate, job);
            if (!result.eligible()) {
                throw new EligibilityException(result.failures());
            }

            Application application = new Application(0, candidate.id(), job.id(),
                    ApplicationStatus.APPLIED, LocalDateTime.now(), candidate.fullName(),
                    job.title(), job.companyName());
            int id = applicationDAO.insert(connection, application);
            auditDAO.insert(connection, AuditLog.of("APPLICATION", id, "SUBMITTED", null,
                    ApplicationStatus.APPLIED.name(), performedBy,
                    "Applied to " + job.title() + " at " + job.companyName()));
            AppLogger.info("Application #" + id + " submitted by candidate #" + candidateId);
            return applicationDAO.findById(connection, id)
                    .orElseThrow(() -> new EntityNotFoundException("Application", id));
        });
    }

    public Application shortlist(int applicationId, String performedBy) throws RPMSException {
        return changeStatus(applicationId, ApplicationStatus.SHORTLISTED, performedBy,
                "Shortlisted by recruiter");
    }

    public Application select(int applicationId, String performedBy) throws RPMSException {
        return changeStatus(applicationId, ApplicationStatus.SELECTED, performedBy,
                "Candidate selected after interview rounds");
    }

    public Application reject(int applicationId, String reason, String performedBy)
            throws RPMSException {
        return changeStatus(applicationId, ApplicationStatus.REJECTED, performedBy,
                reason == null || reason.isBlank() ? "Application rejected" : reason.trim());
    }

    public Application hold(int applicationId, String reason, String performedBy)
            throws RPMSException {
        return changeStatus(applicationId, ApplicationStatus.ON_HOLD, performedBy,
                reason == null || reason.isBlank() ? "Application put on hold" : reason.trim());
    }

    public Application withdraw(int applicationId, int candidateId, String performedBy)
            throws RPMSException {
        return Db.transaction(connection -> {
            Application application = applicationDAO.findById(connection, applicationId)
                    .orElseThrow(() -> new EntityNotFoundException("Application", applicationId));
            if (application.candidateId() != candidateId) {
                throw new ValidationException("You can withdraw only your own application");
            }
            return transition(connection, application, ApplicationStatus.WITHDRAWN,
                    performedBy, "Application withdrawn by candidate");
        });
    }

    public Application changeStatus(int applicationId, ApplicationStatus target,
                                    String performedBy, String details) throws RPMSException {
        if (!MANUAL_STAFF_STATUSES.contains(target)) {
            throw new ValidationException("Status " + target
                    + " must be changed through its dedicated workflow step");
        }
        return Db.transaction(connection -> {
            Application application = applicationDAO.findById(connection, applicationId)
                    .orElseThrow(() -> new EntityNotFoundException("Application", applicationId));
            return transition(connection, application, target, performedBy, details);
        });
    }

    public List<Candidate> eligibleCandidates(int jobId) throws RPMSException {
        return Db.read(connection -> {
            Job job = jobDAO.findById(connection, jobId)
                    .orElseThrow(() -> new EntityNotFoundException("Job", jobId));
            return candidateDAO.findAll(connection).stream()
                    .filter(candidate -> eligibilityEngine.check(candidate, job).eligible())
                    .toList();
        });
    }

    public Application findById(int applicationId) throws RPMSException {
        return Db.read(connection -> applicationDAO.findById(connection, applicationId))
                .orElseThrow(() -> new EntityNotFoundException("Application", applicationId));
    }

    public List<Application> findAll() throws RPMSException {
        return Db.read(applicationDAO::findAll);
    }

    public List<Application> findForCandidate(int candidateId) throws RPMSException {
        return Db.read(connection -> applicationDAO.findByCandidate(connection, candidateId));
    }

    public List<Application> findForJob(int jobId) throws RPMSException {
        return Db.read(connection -> applicationDAO.findByJob(connection, jobId));
    }

    public List<Application> findByStatus(ApplicationStatus status) throws RPMSException {
        return Db.read(connection -> applicationDAO.findByStatus(connection, status));
    }

    private Application transition(java.sql.Connection connection, Application application,
                                   ApplicationStatus target, String performedBy, String details)
            throws RPMSException, java.sql.SQLException {
        if (!application.status().canTransitionTo(target)) {
            throw new InvalidStateTransitionException("Application #" + application.id()
                    + " cannot move from " + application.status() + " to " + target);
        }
        applicationDAO.updateStatus(connection, application.id(), target);
        auditDAO.insert(connection, AuditLog.of("APPLICATION", application.id(), "STATUS_CHANGED",
                application.status().name(), target.name(), performedBy, details));
        return applicationDAO.findById(connection, application.id())
                .orElseThrow(() -> new EntityNotFoundException("Application", application.id()));
    }
}
