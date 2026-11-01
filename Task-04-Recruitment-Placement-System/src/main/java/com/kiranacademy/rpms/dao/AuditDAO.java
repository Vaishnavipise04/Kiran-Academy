package com.kiranacademy.rpms.dao;

import com.kiranacademy.rpms.model.AuditLog;
import com.kiranacademy.rpms.utility.Jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {

    private static final String COLUMNS = "id, entity_name, entity_id, action, old_status, "
            + "new_status, performed_by, details, created_at";

    public int insert(Connection connection, AuditLog log) throws SQLException {
        String sql = "INSERT INTO audit_log (entity_name, entity_id, action, old_status, new_status, "
                + "performed_by, details) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, log.entityName());
            ps.setInt(2, log.entityId());
            ps.setString(3, log.action());
            ps.setString(4, log.oldStatus());
            ps.setString(5, log.newStatus());
            ps.setString(6, log.performedBy());
            ps.setString(7, log.details());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Audit insert did not return a generated id");
    }

    public List<AuditLog> findRecent(Connection connection, int limit) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM audit_log ORDER BY id DESC LIMIT ?";
        List<AuditLog> logs = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(map(rs));
                }
            }
        }
        return logs;
    }

    public List<AuditLog> findByEntity(Connection connection, String entityName, int entityId)
            throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM audit_log WHERE entity_name = ? AND entity_id = ? "
                + "ORDER BY id";
        List<AuditLog> logs = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entityName);
            ps.setInt(2, entityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(map(rs));
                }
            }
        }
        return logs;
    }

    private AuditLog map(ResultSet rs) throws SQLException {
        return new AuditLog(
                rs.getInt("id"),
                rs.getString("entity_name"),
                rs.getInt("entity_id"),
                rs.getString("action"),
                rs.getString("old_status"),
                rs.getString("new_status"),
                rs.getString("performed_by"),
                rs.getString("details"),
                Jdbc.dateTime(rs, "created_at")
        );
    }
}
