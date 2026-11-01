package com.kiranacademy.rpms.service;

import com.kiranacademy.rpms.dao.AuditDAO;
import com.kiranacademy.rpms.dao.CompanyDAO;
import com.kiranacademy.rpms.dao.JobDAO;
import com.kiranacademy.rpms.exception.EntityNotFoundException;
import com.kiranacademy.rpms.exception.InvalidStateTransitionException;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.exception.ValidationException;
import com.kiranacademy.rpms.model.AuditLog;
import com.kiranacademy.rpms.model.Company;
import com.kiranacademy.rpms.model.CompanyStatus;
import com.kiranacademy.rpms.model.Job;
import com.kiranacademy.rpms.model.JobStatus;
import com.kiranacademy.rpms.utility.Db;
import com.kiranacademy.rpms.utility.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class JobService {

    public record JobInput(
            int companyId,
            String title,
            String technology,
            String qualificationRequired,
            BigDecimal minPercentage,
            int passingYearFrom,
            int maxBacklogs,
            int vacancies,
            BigDecimal packageLpa,
            String location,
            LocalDate applicationDeadline
    ) {
    }

    private final JobDAO jobDAO;
    private final CompanyDAO companyDAO;
    private final AuditDAO auditDAO;

    public JobService(JobDAO jobDAO, CompanyDAO companyDAO, AuditDAO auditDAO) {
        this.jobDAO = jobDAO;
        this.companyDAO = companyDAO;
        this.auditDAO = auditDAO;
    }

    public Job create(JobInput input, String performedBy) throws RPMSException {
        validate(input);
        return Db.transaction(connection -> {
            Company company = companyDAO.findById(connection, input.companyId())
                    .orElseThrow(() -> new EntityNotFoundException("Company", input.companyId()));
            if (company.status() != CompanyStatus.ACTIVE) {
                throw new ValidationException("Cannot create a job for an inactive company");
            }
            Job job = new Job(0, company.id(), company.name(), input.title().trim(),
                    input.technology().trim(), input.qualificationRequired().trim(),
                    input.minPercentage(), input.passingYearFrom(), input.maxBacklogs(),
                    input.vacancies(), input.packageLpa(), input.location().trim(),
                    JobStatus.OPEN, input.applicationDeadline(), LocalDateTime.now());
            int id = jobDAO.insert(connection, job);
            auditDAO.insert(connection, AuditLog.of("JOB", id, "CREATED", null, JobStatus.OPEN.name(),
                    performedBy, "Job '" + job.title() + "' created for " + company.name()));
            return jobDAO.findById(connection, id)
                    .orElseThrow(() -> new EntityNotFoundException("Job", id));
        });
    }

    public Job update(int jobId, JobInput input, String performedBy) throws RPMSException {
        validate(input);
        return Db.transaction(connection -> {
            Job existing = jobDAO.findById(connection, jobId)
                    .orElseThrow(() -> new EntityNotFoundException("Job", jobId));
            Company company = companyDAO.findById(connection, input.companyId())
                    .orElseThrow(() -> new EntityNotFoundException("Company", input.companyId()));
            Job updated = new Job(existing.id(), company.id(), company.name(), input.title().trim(),
                    input.technology().trim(), input.qualificationRequired().trim(),
                    input.minPercentage(), input.passingYearFrom(), input.maxBacklogs(),
                    input.vacancies(), input.packageLpa(), input.location().trim(),
                    existing.status(), input.applicationDeadline(), existing.createdAt());
            jobDAO.update(connection, updated);
            auditDAO.insert(connection, AuditLog.of("JOB", jobId, "UPDATED", existing.status().name(),
                    existing.status().name(), performedBy, "Job details updated"));
            return jobDAO.findById(connection, jobId)
                    .orElseThrow(() -> new EntityNotFoundException("Job", jobId));
        });
    }

    public Job setStatus(int jobId, JobStatus target, String performedBy) throws RPMSException {
        return Db.transaction(connection -> {
            Job job = jobDAO.findById(connection, jobId)
                    .orElseThrow(() -> new EntityNotFoundException("Job", jobId));
            if (!job.status().canTransitionTo(target)) {
                throw new InvalidStateTransitionException(
                        "Job status cannot change from " + job.status() + " to " + target);
            }
            jobDAO.updateStatus(connection, jobId, target);
            auditDAO.insert(connection, AuditLog.of("JOB", jobId, "STATUS_CHANGED",
                    job.status().name(), target.name(), performedBy, "Job status updated"));
            return jobDAO.findById(connection, jobId)
                    .orElseThrow(() -> new EntityNotFoundException("Job", jobId));
        });
    }

    public Job findById(int jobId) throws RPMSException {
        return Db.read(connection -> jobDAO.findById(connection, jobId))
                .orElseThrow(() -> new EntityNotFoundException("Job", jobId));
    }

    public List<Job> findAll(boolean openOnly) throws RPMSException {
        return Db.read(connection -> jobDAO.findAll(connection, openOnly));
    }

    public List<Job> findByCompany(int companyId) throws RPMSException {
        return Db.read(connection -> jobDAO.findByCompany(connection, companyId));
    }

    public List<Job> searchByTechnology(String technology) throws RPMSException {
        if (technology == null || technology.isBlank()) {
            return findAll(true);
        }
        return Db.read(connection -> jobDAO.searchByTechnology(connection, technology.trim()));
    }

    private void validate(JobInput input) throws ValidationException {
        if (input == null) {
            throw new ValidationException("Job details are required");
        }
        ValidationUtil.requiredText(input.title(), "Job title", 2, 120);
        ValidationUtil.requiredText(input.technology(), "Technology", 2, 150);
        ValidationUtil.requiredText(input.qualificationRequired(), "Qualification requirement", 2, 120);
        ValidationUtil.percentage(input.minPercentage());
        ValidationUtil.passingYear(input.passingYearFrom());
        ValidationUtil.nonNegative(input.maxBacklogs(), "Maximum backlogs");
        ValidationUtil.positive(input.vacancies(), "Vacancies");
        ValidationUtil.positive(input.packageLpa(), "Package");
        ValidationUtil.requiredText(input.location(), "Location", 2, 100);
        if (input.applicationDeadline() == null) {
            throw new ValidationException("Application deadline is required");
        }
        if (input.applicationDeadline().isBefore(LocalDate.now())) {
            throw new ValidationException("Application deadline cannot be in the past");
        }
    }
}
