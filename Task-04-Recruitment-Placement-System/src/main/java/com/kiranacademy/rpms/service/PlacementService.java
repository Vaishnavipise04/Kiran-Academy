package com.kiranacademy.rpms.service;

import com.kiranacademy.rpms.dao.AuditDAO;
import com.kiranacademy.rpms.dao.PlacementDAO;
import com.kiranacademy.rpms.exception.EntityNotFoundException;
import com.kiranacademy.rpms.exception.InvalidStateTransitionException;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.model.AuditLog;
import com.kiranacademy.rpms.model.Placement;
import com.kiranacademy.rpms.model.PlacementStatus;
import com.kiranacademy.rpms.utility.Db;

import java.util.List;

public class PlacementService {

    private final PlacementDAO placementDAO;
    private final AuditDAO auditDAO;

    public PlacementService(PlacementDAO placementDAO, AuditDAO auditDAO) {
        this.placementDAO = placementDAO;
        this.auditDAO = auditDAO;
    }

    public List<Placement> findAll() throws RPMSException {
        return Db.read(placementDAO::findAll);
    }

    public Placement findById(int placementId) throws RPMSException {
        return Db.read(connection -> placementDAO.findById(connection, placementId))
                .orElseThrow(() -> new EntityNotFoundException("Placement", placementId));
    }

    public Placement findByCandidate(int candidateId) throws RPMSException {
        return Db.read(connection -> placementDAO.findByCandidate(connection, candidateId))
                .orElseThrow(() -> new EntityNotFoundException(
                        "Placement for candidate", candidateId));
    }

    public Placement markCompleted(int placementId, String performedBy) throws RPMSException {
        return Db.transaction(connection -> {
            Placement placement = placementDAO.findById(connection, placementId)
                    .orElseThrow(() -> new EntityNotFoundException("Placement", placementId));
            if (placement.status() != PlacementStatus.ACTIVE) {
                throw new InvalidStateTransitionException(
                        "Placement is already " + placement.status());
            }
            placementDAO.updateStatus(connection, placementId, PlacementStatus.COMPLETED);
            auditDAO.insert(connection, AuditLog.of("PLACEMENT", placementId, "STATUS_CHANGED",
                    PlacementStatus.ACTIVE.name(), PlacementStatus.COMPLETED.name(), performedBy,
                    "Placement marked as completed"));
            return placementDAO.findById(connection, placementId)
                    .orElseThrow(() -> new EntityNotFoundException("Placement", placementId));
        });
    }
}
