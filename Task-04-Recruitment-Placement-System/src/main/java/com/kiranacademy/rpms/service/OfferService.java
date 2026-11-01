package com.kiranacademy.rpms.service;

import com.kiranacademy.rpms.dao.ApplicationDAO;
import com.kiranacademy.rpms.dao.AuditDAO;
import com.kiranacademy.rpms.dao.CandidateDAO;
import com.kiranacademy.rpms.dao.JobDAO;
import com.kiranacademy.rpms.dao.OfferDAO;
import com.kiranacademy.rpms.dao.PlacementDAO;
import com.kiranacademy.rpms.exception.EntityNotFoundException;
import com.kiranacademy.rpms.exception.InvalidStateTransitionException;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.exception.ValidationException;
import com.kiranacademy.rpms.model.Application;
import com.kiranacademy.rpms.model.ApplicationStatus;
import com.kiranacademy.rpms.model.AuditLog;
import com.kiranacademy.rpms.model.Candidate;
import com.kiranacademy.rpms.model.CandidateStatus;
import com.kiranacademy.rpms.model.Job;
import com.kiranacademy.rpms.model.Offer;
import com.kiranacademy.rpms.model.OfferStatus;
import com.kiranacademy.rpms.model.Placement;
import com.kiranacademy.rpms.model.PlacementStatus;
import com.kiranacademy.rpms.utility.AppLogger;
import com.kiranacademy.rpms.utility.Db;
import com.kiranacademy.rpms.utility.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OfferService {

    private final OfferDAO offerDAO;
    private final ApplicationDAO applicationDAO;
    private final CandidateDAO candidateDAO;
    private final JobDAO jobDAO;
    private final PlacementDAO placementDAO;
    private final AuditDAO auditDAO;

    public OfferService(OfferDAO offerDAO, ApplicationDAO applicationDAO, CandidateDAO candidateDAO,
                        JobDAO jobDAO, PlacementDAO placementDAO, AuditDAO auditDAO) {
        this.offerDAO = offerDAO;
        this.applicationDAO = applicationDAO;
        this.candidateDAO = candidateDAO;
        this.jobDAO = jobDAO;
        this.placementDAO = placementDAO;
        this.auditDAO = auditDAO;
    }

    public Offer createOffer(int applicationId, BigDecimal offeredCtc, LocalDate offerDate,
                             LocalDate joiningDate, String performedBy) throws RPMSException {
        ValidationUtil.positive(offeredCtc, "Offered CTC");
        if (offerDate == null) {
            throw new ValidationException("Offer date is required");
        }
        if (offerDate.isAfter(LocalDate.now())) {
            throw new ValidationException("Offer date cannot be in the future");
        }
        if (joiningDate == null) {
            throw new ValidationException("Joining date is required");
        }
        if (joiningDate.isBefore(offerDate)) {
            throw new ValidationException("Joining date cannot be before the offer date");
        }
        return Db.transaction(connection -> {
            Application application = applicationDAO.findById(connection, applicationId)
                    .orElseThrow(() -> new EntityNotFoundException("Application", applicationId));
            if (application.status() != ApplicationStatus.SELECTED) {
                throw new InvalidStateTransitionException("Offer can be created only for a SELECTED "
                        + "application. Current status: " + application.status());
            }
            if (offerDAO.findByApplication(connection, applicationId).isPresent()) {
                throw new ValidationException("An offer already exists for this application");
            }

            Offer offer = new Offer(0, applicationId, offeredCtc, offerDate, OfferStatus.PENDING,
                    joiningDate, LocalDateTime.now(), application.candidateName(),
                    application.jobTitle(), application.companyName());
            int id = offerDAO.insert(connection, offer);

            applicationDAO.updateStatus(connection, applicationId, ApplicationStatus.OFFERED);
            auditDAO.insert(connection, AuditLog.of("APPLICATION", applicationId, "STATUS_CHANGED",
                    ApplicationStatus.SELECTED.name(), ApplicationStatus.OFFERED.name(), performedBy,
                    "Offer " + offeredCtc + " LPA issued"));
            auditDAO.insert(connection, AuditLog.of("OFFER", id, "CREATED", null,
                    OfferStatus.PENDING.name(), performedBy,
                    "Offer issued to " + application.candidateName()));

            return offerDAO.findById(connection, id)
                    .orElseThrow(() -> new EntityNotFoundException("Offer", id));
        });
    }

    public Offer respond(int offerId, boolean accept, int candidateId, String performedBy)
            throws RPMSException {
        return Db.transaction(connection -> {
            Offer offer = offerDAO.findById(connection, offerId)
                    .orElseThrow(() -> new EntityNotFoundException("Offer", offerId));
            Application application = applicationDAO.findById(connection, offer.applicationId())
                    .orElseThrow(() -> new EntityNotFoundException("Application",
                            offer.applicationId()));
            if (application.candidateId() != candidateId) {
                throw new ValidationException("You can respond only to your own offer");
            }
            if (offer.status() != OfferStatus.PENDING) {
                throw new InvalidStateTransitionException(
                        "Offer is already " + offer.status() + " and cannot be changed");
            }

            OfferStatus target = accept ? OfferStatus.ACCEPTED : OfferStatus.DECLINED;
            if (!offer.status().canTransitionTo(target)) {
                throw new InvalidStateTransitionException(
                        "Offer cannot move from " + offer.status() + " to " + target);
            }
            offerDAO.updateStatus(connection, offerId, target);
            auditDAO.insert(connection, AuditLog.of("OFFER", offerId, "CANDIDATE_RESPONSE",
                    OfferStatus.PENDING.name(), target.name(), performedBy,
                    accept ? "Offer accepted by candidate" : "Offer declined by candidate"));

            if (!accept) {
                applicationDAO.updateStatus(connection, application.id(), ApplicationStatus.REJECTED);
                auditDAO.insert(connection, AuditLog.of("APPLICATION", application.id(),
                        "STATUS_CHANGED", ApplicationStatus.OFFERED.name(),
                        ApplicationStatus.REJECTED.name(), performedBy,
                        "Candidate declined the offer"));
            }
            return offerDAO.findById(connection, offerId)
                    .orElseThrow(() -> new EntityNotFoundException("Offer", offerId));
        });
    }

    public Offer revoke(int offerId, String reason, String performedBy) throws RPMSException {
        return Db.transaction(connection -> {
            Offer offer = offerDAO.findById(connection, offerId)
                    .orElseThrow(() -> new EntityNotFoundException("Offer", offerId));
            if (!offer.status().canTransitionTo(OfferStatus.REVOKED)) {
                throw new InvalidStateTransitionException("Offer cannot be revoked from status "
                        + offer.status());
            }
            offerDAO.updateStatus(connection, offerId, OfferStatus.REVOKED);
            auditDAO.insert(connection, AuditLog.of("OFFER", offerId, "REVOKED",
                    OfferStatus.PENDING.name(), OfferStatus.REVOKED.name(), performedBy,
                    reason == null || reason.isBlank() ? "Offer revoked" : reason.trim()));

            Application application = applicationDAO.findById(connection, offer.applicationId())
                    .orElseThrow(() -> new EntityNotFoundException("Application",
                            offer.applicationId()));
            if (application.status().canTransitionTo(ApplicationStatus.REJECTED)) {
                applicationDAO.updateStatus(connection, application.id(), ApplicationStatus.REJECTED);
                auditDAO.insert(connection, AuditLog.of("APPLICATION", application.id(),
                        "STATUS_CHANGED", application.status().name(),
                        ApplicationStatus.REJECTED.name(), performedBy, "Offer revoked by recruiter"));
            }
            return offerDAO.findById(connection, offerId)
                    .orElseThrow(() -> new EntityNotFoundException("Offer", offerId));
        });
    }

    public Placement recordJoining(int offerId, LocalDate placementDate, String performedBy)
            throws RPMSException {
        if (placementDate == null) {
            throw new ValidationException("Placement date is required");
        }
        return Db.transaction(connection -> {
            Offer offer = offerDAO.findById(connection, offerId)
                    .orElseThrow(() -> new EntityNotFoundException("Offer", offerId));
            if (offer.status() != OfferStatus.ACCEPTED) {
                throw new InvalidStateTransitionException(
                        "Joining can be recorded only for an ACCEPTED offer. Current: " + offer.status());
            }
            if (placementDate.isBefore(offer.offerDate())) {
                throw new ValidationException("Placement date cannot be before the offer date");
            }
            Application application = applicationDAO.findById(connection, offer.applicationId())
                    .orElseThrow(() -> new EntityNotFoundException("Application",
                            offer.applicationId()));
            if (application.status() != ApplicationStatus.OFFERED) {
                throw new InvalidStateTransitionException("Joining can be recorded only while the "
                        + "application is OFFERED. Current: " + application.status());
            }
            if (placementDAO.existsByCandidate(connection, application.candidateId())) {
                throw new ValidationException("A placement already exists for this candidate");
            }
            Candidate candidate = candidateDAO.findById(connection, application.candidateId())
                    .orElseThrow(() -> new EntityNotFoundException("Candidate",
                            application.candidateId()));
            if (candidate.status() != CandidateStatus.ACTIVE) {
                throw new InvalidStateTransitionException(
                        "Candidate is " + candidate.status() + " and cannot be placed");
            }
            Job job = jobDAO.findById(connection, application.jobId())
                    .orElseThrow(() -> new EntityNotFoundException("Job", application.jobId()));

            Placement placement = new Placement(0, candidate.id(), job.companyId(), job.id(),
                    offer.id(), placementDate, offer.offeredCtc(), PlacementStatus.ACTIVE,
                    LocalDateTime.now(), candidate.fullName(), job.companyName(), job.title());
            int placementId = placementDAO.insert(connection, placement);

            applicationDAO.updateStatus(connection, application.id(), ApplicationStatus.JOINED);
            auditDAO.insert(connection, AuditLog.of("APPLICATION", application.id(),
                    "STATUS_CHANGED", ApplicationStatus.OFFERED.name(),
                    ApplicationStatus.JOINED.name(), performedBy,
                    "Candidate joined " + job.companyName()));

            candidateDAO.updateStatus(connection, candidate.id(), CandidateStatus.PLACED);
            auditDAO.insert(connection, AuditLog.of("CANDIDATE", candidate.id(),
                    "STATUS_CHANGED", CandidateStatus.ACTIVE.name(),
                    CandidateStatus.PLACED.name(), performedBy,
                    "Placed at " + job.companyName()));
            auditDAO.insert(connection, AuditLog.of("PLACEMENT", placementId, "CREATED", null,
                    PlacementStatus.ACTIVE.name(), performedBy,
                    candidate.fullName() + " placed at " + job.companyName()));

            AppLogger.info("Placement recorded for candidate #" + candidate.id()
                    + " at " + job.companyName());
            return placementDAO.findById(connection, placementId)
                    .orElseThrow(() -> new EntityNotFoundException("Placement", placementId));
        });
    }

    public Offer findById(int offerId) throws RPMSException {
        return Db.read(connection -> offerDAO.findById(connection, offerId))
                .orElseThrow(() -> new EntityNotFoundException("Offer", offerId));
    }

    public List<Offer> findAll() throws RPMSException {
        return Db.read(offerDAO::findAll);
    }

    public List<Offer> findForCandidate(int candidateId) throws RPMSException {
        return Db.read(connection -> offerDAO.findByCandidate(connection, candidateId));
    }
}
