package com.kiranacademy.rpms.model;

import java.time.LocalDateTime;

public record AuditLog(
        int id,
        String entityName,
        int entityId,
        String action,
        String oldStatus,
        String newStatus,
        String performedBy,
        String details,
        LocalDateTime createdAt
) {
    public static AuditLog of(String entityName, int entityId, String action,
                              String oldStatus, String newStatus,
                              String performedBy, String details) {
        return new AuditLog(0, entityName, entityId, action, oldStatus, newStatus,
                performedBy, details, LocalDateTime.now());
    }
}
