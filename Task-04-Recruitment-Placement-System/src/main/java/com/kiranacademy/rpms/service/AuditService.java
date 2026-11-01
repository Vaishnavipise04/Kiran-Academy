package com.kiranacademy.rpms.service;

import com.kiranacademy.rpms.dao.AuditDAO;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.model.AuditLog;
import com.kiranacademy.rpms.utility.Db;

import java.util.List;

public class AuditService {

    private final AuditDAO auditDAO;

    public AuditService(AuditDAO auditDAO) {
        this.auditDAO = auditDAO;
    }

    public List<AuditLog> recent(int limit) throws RPMSException {
        return Db.read(connection -> auditDAO.findRecent(connection, limit));
    }

    public List<AuditLog> forEntity(String entityName, int entityId) throws RPMSException {
        return Db.read(connection -> auditDAO.findByEntity(connection, entityName.toUpperCase(), entityId));
    }
}
