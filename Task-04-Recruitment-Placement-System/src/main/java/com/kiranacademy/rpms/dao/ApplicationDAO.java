package com.kiranacademy.rpms.dao;

import com.kiranacademy.rpms.model.Application;
import com.kiranacademy.rpms.model.ApplicationStatus;
import com.kiranacademy.rpms.utility.Jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApplicationDAO {

    private static final String SELECT = "SELECT a.id, a.candidate_id, a.job_id, a.status, a.applied_at, "
            + "CONCAT(c.first_name, ' ', c.last_name) AS candidate_name, "
            + "j.title AS job_title, co.name AS company_name "
            + "FROM applications a "
            + "JOIN candidates c ON c.id = a.candidate_id "
            + "JOIN jobs j ON j.id = a.job_id "
            + "JOIN companies co ON co.id = j.company_id ";

    public int insert(Connection connection, Application application) throws SQLException {
        String sql = "INSERT INTO applications (candidate_id, job_id, status, applied_at) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, application.candidateId());
            ps.setInt(2, application.jobId());
            ps.setString(3, application.status().name());
            Jdbc.setDateTime(ps, 4, application.appliedAt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Application insert did not return a generated id");
    }

    public Optional<Application> findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT + "WHERE a.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Application> findByCandidateAndJob(Connection connection, int candidateId, int jobId)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                SELECT + "WHERE a.candidate_id = ? AND a.job_id = ?")) {
            ps.setInt(1, candidateId);
            ps.setInt(2, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Application> findAll(Connection connection) throws SQLException {
        return findMany(connection, SELECT + "ORDER BY a.applied_at DESC, a.id DESC");
    }

    public List<Application> findByCandidate(Connection connection, int candidateId) throws SQLException {
        return findMany(connection, SELECT + "WHERE a.candidate_id = ? ORDER BY a.id DESC", candidateId);
    }

    public List<Application> findByJob(Connection connection, int jobId) throws SQLException {
        return findMany(connection, SELECT + "WHERE a.job_id = ? ORDER BY a.id", jobId);
    }

    public List<Application> findByStatus(Connection connection, ApplicationStatus status)
            throws SQLException {
        String sql = SELECT + "WHERE a.status = ? ORDER BY a.id";
        List<Application> applications = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    applications.add(map(rs));
                }
            }
        }
        return applications;
    }

    public void updateStatus(Connection connection, int applicationId, ApplicationStatus status)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE applications SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, applicationId);
            ps.executeUpdate();
        }
    }

    public long countAll(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM applications");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public long countByStatus(Connection connection, ApplicationStatus status) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM applications WHERE status = ?")) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private List<Application> findMany(Connection connection, String sql, Integer... params)
            throws SQLException {
        List<Application> applications = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    applications.add(map(rs));
                }
            }
        }
        return applications;
    }

    private Application map(ResultSet rs) throws SQLException {
        return new Application(
                rs.getInt("id"),
                rs.getInt("candidate_id"),
                rs.getInt("job_id"),
                ApplicationStatus.valueOf(rs.getString("status")),
                Jdbc.dateTime(rs, "applied_at"),
                rs.getString("candidate_name"),
                rs.getString("job_title"),
                rs.getString("company_name")
        );
    }
}
