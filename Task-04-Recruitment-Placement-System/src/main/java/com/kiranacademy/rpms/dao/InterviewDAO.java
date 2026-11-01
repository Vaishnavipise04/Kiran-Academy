package com.kiranacademy.rpms.dao;

import com.kiranacademy.rpms.model.Interview;
import com.kiranacademy.rpms.model.InterviewMode;
import com.kiranacademy.rpms.model.InterviewStatus;
import com.kiranacademy.rpms.utility.Jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InterviewDAO {

    private static final String SELECT = "SELECT i.id, i.application_id, i.round_no, i.round_type, "
            + "i.scheduled_at, i.interviewer, i.mode, i.status, i.created_at, "
            + "CONCAT(c.first_name, ' ', c.last_name) AS candidate_name, "
            + "j.title AS job_title, co.name AS company_name "
            + "FROM interviews i "
            + "JOIN applications a ON a.id = i.application_id "
            + "JOIN candidates c ON c.id = a.candidate_id "
            + "JOIN jobs j ON j.id = a.job_id "
            + "JOIN companies co ON co.id = j.company_id ";

    public int insert(Connection connection, Interview interview) throws SQLException {
        String sql = "INSERT INTO interviews (application_id, round_no, round_type, scheduled_at, "
                + "interviewer, mode, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, interview.applicationId());
            ps.setInt(2, interview.roundNo());
            ps.setString(3, interview.roundType());
            Jdbc.setDateTime(ps, 4, interview.scheduledAt());
            ps.setString(5, interview.interviewer());
            ps.setString(6, interview.mode().name());
            ps.setString(7, interview.status().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Interview insert did not return a generated id");
    }

    public Optional<Interview> findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT + "WHERE i.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Interview> findAll(Connection connection) throws SQLException {
        return findMany(connection, SELECT + "ORDER BY i.scheduled_at DESC, i.id DESC");
    }

    public List<Interview> findByApplication(Connection connection, int applicationId) throws SQLException {
        return findMany(connection, SELECT + "WHERE i.application_id = ? ORDER BY i.round_no",
                applicationId);
    }

    public List<Interview> findByCandidate(Connection connection, int candidateId) throws SQLException {
        return findMany(connection, SELECT + "WHERE a.candidate_id = ? ORDER BY i.scheduled_at DESC",
                candidateId);
    }

    public List<Interview> findUpcoming(Connection connection) throws SQLException {
        return findMany(connection,
                SELECT + "WHERE i.status = 'SCHEDULED' AND i.scheduled_at >= NOW() "
                        + "ORDER BY i.scheduled_at");
    }

    public void updateStatus(Connection connection, int interviewId, InterviewStatus status)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE interviews SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, interviewId);
            ps.executeUpdate();
        }
    }

    public int nextRoundNo(Connection connection, int applicationId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COALESCE(MAX(round_no), 0) + 1 FROM interviews WHERE application_id = ?")) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private List<Interview> findMany(Connection connection, String sql, Integer... params)
            throws SQLException {
        List<Interview> interviews = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    interviews.add(map(rs));
                }
            }
        }
        return interviews;
    }

    private Interview map(ResultSet rs) throws SQLException {
        return new Interview(
                rs.getInt("id"),
                rs.getInt("application_id"),
                rs.getInt("round_no"),
                rs.getString("round_type"),
                Jdbc.dateTime(rs, "scheduled_at"),
                rs.getString("interviewer"),
                InterviewMode.valueOf(rs.getString("mode")),
                InterviewStatus.valueOf(rs.getString("status")),
                Jdbc.dateTime(rs, "created_at"),
                rs.getString("candidate_name"),
                rs.getString("job_title"),
                rs.getString("company_name")
        );
    }
}
