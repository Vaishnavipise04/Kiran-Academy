package com.kiranacademy.rpms.service;

import com.kiranacademy.rpms.dao.AuditDAO;
import com.kiranacademy.rpms.dao.CandidateDAO;
import com.kiranacademy.rpms.exception.EntityNotFoundException;
import com.kiranacademy.rpms.exception.InvalidStateTransitionException;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.exception.ValidationException;
import com.kiranacademy.rpms.model.AuditLog;
import com.kiranacademy.rpms.model.Candidate;
import com.kiranacademy.rpms.model.CandidateStatus;
import com.kiranacademy.rpms.utility.AppLogger;
import com.kiranacademy.rpms.utility.Db;
import com.kiranacademy.rpms.utility.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

public class CandidateService {

    public record CandidateInput(
            String firstName,
            String lastName,
            String email,
            String phone,
            String qualification,
            int passingYear,
            BigDecimal percentage,
            String technology,
            int backlogs,
            String skills
    ) {
    }

    private final CandidateDAO candidateDAO;
    private final AuditDAO auditDAO;

    public CandidateService(CandidateDAO candidateDAO, AuditDAO auditDAO) {
        this.candidateDAO = candidateDAO;
        this.auditDAO = auditDAO;
    }

    public Candidate register(CandidateInput input, String performedBy) throws RPMSException {
        validate(input);
        return Db.transaction(connection -> {
            if (candidateDAO.existsByEmail(connection, input.email())) {
                throw new ValidationException("A candidate with this email already exists");
            }
            if (candidateDAO.existsByPhone(connection, input.phone())) {
                throw new ValidationException("A candidate with this phone number already exists");
            }
            int sequence = candidateDAO.nextSequence(connection);
            String registrationNo = "KA" + Year.now().getValue() + String.format("%04d", sequence);
            Candidate candidate = new Candidate(0, registrationNo,
                    input.firstName().trim(), input.lastName().trim(), input.email().trim(),
                    input.phone().trim(), input.qualification().trim(), input.passingYear(),
                    input.percentage(), input.technology().trim(), input.backlogs(),
                    input.skills() == null ? null : input.skills().trim(),
                    CandidateStatus.ACTIVE, LocalDateTime.now());
            int id = candidateDAO.insert(connection, candidate);
            auditDAO.insert(connection, AuditLog.of("CANDIDATE", id, "REGISTERED",
                    null, CandidateStatus.ACTIVE.name(), performedBy,
                    "Candidate " + registrationNo + " registered"));
            AppLogger.info("Candidate registered: " + registrationNo + " by " + performedBy);
            return candidateDAO.findById(connection, id)
                    .orElseThrow(() -> new EntityNotFoundException("Candidate", id));
        });
    }

    public Candidate update(int candidateId, CandidateInput input, String performedBy)
            throws RPMSException {
        validate(input);
        return Db.transaction(connection -> {
            Candidate existing = candidateDAO.findById(connection, candidateId)
                    .orElseThrow(() -> new EntityNotFoundException("Candidate", candidateId));
            if (!existing.email().equalsIgnoreCase(input.email())
                    && candidateDAO.existsByEmail(connection, input.email())) {
                throw new ValidationException("A candidate with this email already exists");
            }
            if (!existing.phone().equals(input.phone())
                    && candidateDAO.existsByPhone(connection, input.phone())) {
                throw new ValidationException("A candidate with this phone number already exists");
            }
            Candidate updated = new Candidate(existing.id(), existing.registrationNo(),
                    input.firstName().trim(), input.lastName().trim(), input.email().trim(),
                    input.phone().trim(), input.qualification().trim(), input.passingYear(),
                    input.percentage(), input.technology().trim(), input.backlogs(),
                    input.skills() == null ? null : input.skills().trim(),
                    existing.status(), existing.createdAt());
            candidateDAO.update(connection, updated);
            auditDAO.insert(connection, AuditLog.of("CANDIDATE", candidateId, "PROFILE_UPDATED",
                    existing.status().name(), existing.status().name(), performedBy,
                    "Candidate profile details updated"));
            return updated;
        });
    }

    public Candidate setStatus(int candidateId, CandidateStatus target, String performedBy)
            throws RPMSException {
        return Db.transaction(connection -> {
            Candidate candidate = candidateDAO.findById(connection, candidateId)
                    .orElseThrow(() -> new EntityNotFoundException("Candidate", candidateId));
            if (target == CandidateStatus.PLACED) {
                throw new ValidationException(
                        "PLACED status is set automatically when an offer joining is recorded");
            }
            if (!candidate.status().canTransitionTo(target)) {
                throw new InvalidStateTransitionException(
                        "Candidate status cannot change from " + candidate.status() + " to " + target);
            }
            candidateDAO.updateStatus(connection, candidateId, target);
            auditDAO.insert(connection, AuditLog.of("CANDIDATE", candidateId, "STATUS_CHANGED",
                    candidate.status().name(), target.name(), performedBy, "Candidate status updated"));
            return candidateDAO.findById(connection, candidateId)
                    .orElseThrow(() -> new EntityNotFoundException("Candidate", candidateId));
        });
    }

    public Candidate findById(int candidateId) throws RPMSException {
        return Db.read(connection -> candidateDAO.findById(connection, candidateId))
                .orElseThrow(() -> new EntityNotFoundException("Candidate", candidateId));
    }

    public List<Candidate> findAll() throws RPMSException {
        return Db.read(candidateDAO::findAll);
    }

    public List<Candidate> search(String keyword) throws RPMSException {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return Db.read(connection -> candidateDAO.search(connection, keyword.trim()));
    }

    private void validate(CandidateInput input) throws ValidationException {
        if (input == null) {
            throw new ValidationException("Candidate details are required");
        }
        ValidationUtil.requiredText(input.firstName(), "First name", 2, 60);
        ValidationUtil.requiredText(input.lastName(), "Last name", 1, 60);
        ValidationUtil.email(input.email());
        ValidationUtil.phone(input.phone());
        ValidationUtil.requiredText(input.qualification(), "Qualification", 2, 60);
        ValidationUtil.passingYear(input.passingYear());
        ValidationUtil.percentage(input.percentage());
        ValidationUtil.requiredText(input.technology(), "Technology", 2, 120);
        ValidationUtil.nonNegative(input.backlogs(), "Backlogs");
        ValidationUtil.optionalText(input.skills(), "Skills", 500);
    }
}
