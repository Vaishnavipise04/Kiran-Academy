package com.kiranacademy.rpms.service;

import com.kiranacademy.rpms.dao.AuditDAO;
import com.kiranacademy.rpms.dao.CompanyDAO;
import com.kiranacademy.rpms.exception.EntityNotFoundException;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.exception.ValidationException;
import com.kiranacademy.rpms.model.AuditLog;
import com.kiranacademy.rpms.model.Company;
import com.kiranacademy.rpms.model.CompanyStatus;
import com.kiranacademy.rpms.utility.Db;
import com.kiranacademy.rpms.utility.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;

public class CompanyService {

    public record CompanyInput(
            String name,
            String industry,
            String location,
            String website,
            String contactPerson,
            String contactEmail
    ) {
    }

    private final CompanyDAO companyDAO;
    private final AuditDAO auditDAO;

    public CompanyService(CompanyDAO companyDAO, AuditDAO auditDAO) {
        this.companyDAO = companyDAO;
        this.auditDAO = auditDAO;
    }

    public Company add(CompanyInput input, String performedBy) throws RPMSException {
        validate(input);
        return Db.transaction(connection -> {
            if (companyDAO.findByName(connection, input.name()).isPresent()) {
                throw new ValidationException("A company with this name already exists");
            }
            Company company = new Company(0, input.name().trim(), input.industry().trim(),
                    input.location().trim(), emptyToNull(input.website()), input.contactPerson().trim(),
                    input.contactEmail().trim(), CompanyStatus.ACTIVE, LocalDateTime.now());
            int id = companyDAO.insert(connection, company);
            auditDAO.insert(connection, AuditLog.of("COMPANY", id, "CREATED", null,
                    CompanyStatus.ACTIVE.name(), performedBy, "Company " + company.name() + " added"));
            return companyDAO.findById(connection, id)
                    .orElseThrow(() -> new EntityNotFoundException("Company", id));
        });
    }

    public Company update(int companyId, CompanyInput input, String performedBy) throws RPMSException {
        validate(input);
        return Db.transaction(connection -> {
            Company existing = companyDAO.findById(connection, companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Company", companyId));
            if (!existing.name().equalsIgnoreCase(input.name())
                    && companyDAO.findByName(connection, input.name()).isPresent()) {
                throw new ValidationException("A company with this name already exists");
            }
            Company updated = new Company(existing.id(), input.name().trim(), input.industry().trim(),
                    input.location().trim(), emptyToNull(input.website()), input.contactPerson().trim(),
                    input.contactEmail().trim(), existing.status(), existing.createdAt());
            companyDAO.update(connection, updated);
            auditDAO.insert(connection, AuditLog.of("COMPANY", companyId, "UPDATED",
                    existing.status().name(), existing.status().name(), performedBy,
                    "Company details updated"));
            return updated;
        });
    }

    public Company setStatus(int companyId, CompanyStatus status, String performedBy)
            throws RPMSException {
        return Db.transaction(connection -> {
            Company company = companyDAO.findById(connection, companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Company", companyId));
            if (company.status() == status) {
                throw new ValidationException("Company is already " + status);
            }
            companyDAO.updateStatus(connection, companyId, status);
            auditDAO.insert(connection, AuditLog.of("COMPANY", companyId, "STATUS_CHANGED",
                    company.status().name(), status.name(), performedBy, "Company status updated"));
            return companyDAO.findById(connection, companyId)
                    .orElseThrow(() -> new EntityNotFoundException("Company", companyId));
        });
    }

    public Company findById(int companyId) throws RPMSException {
        return Db.read(connection -> companyDAO.findById(connection, companyId))
                .orElseThrow(() -> new EntityNotFoundException("Company", companyId));
    }

    public List<Company> findAll() throws RPMSException {
        return Db.read(companyDAO::findAll);
    }

    private void validate(CompanyInput input) throws ValidationException {
        if (input == null) {
            throw new ValidationException("Company details are required");
        }
        ValidationUtil.requiredText(input.name(), "Company name", 2, 120);
        ValidationUtil.requiredText(input.industry(), "Industry", 2, 80);
        ValidationUtil.requiredText(input.location(), "Location", 2, 100);
        ValidationUtil.optionalText(input.website(), "Website", 150);
        ValidationUtil.requiredText(input.contactPerson(), "Contact person", 2, 100);
        ValidationUtil.email(input.contactEmail());
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
