package com.kiranacademy.rpms.dao;

import com.kiranacademy.rpms.model.InterviewFeedback;
import com.kiranacademy.rpms.model.InterviewResult;
import com.kiranacademy.rpms.utility.Jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeedbackDAO {

    private static final String COLUMNS = "id, interview_id, technical_rating, communication_rating, "
            + "attitude_rating, comments, result, created_by, created_at";

    public int insert(Connection connection, InterviewFeedback feedback) throws SQLException {
        String sql = "INSERT INTO interview_feedback (interview_id, technical_rating, "
                + "communication_rating, attitude_rating, comments, result, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, feedback.interviewId());
            ps.setInt(2, feedback.technicalRating());
            ps.setInt(3, feedback.communicationRating());
            ps.setInt(4, feedback.attitudeRating());
            ps.setString(5, feedback.comments());
            ps.setString(6, feedback.result().name());
            ps.setString(7, feedback.createdBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Feedback insert did not return a generated id");
    }

    public Optional<InterviewFeedback> findByInterview(Connection connection, int interviewId)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT " + COLUMNS + " FROM interview_feedback WHERE interview_id = ?")) {
            ps.setInt(1, interviewId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<InterviewFeedback> findByApplication(Connection connection, int applicationId)
            throws SQLException {
        String sql = "SELECT f.id, f.interview_id, f.technical_rating, f.communication_rating, "
                + "f.attitude_rating, f.comments, f.result, f.created_by, f.created_at "
                + "FROM interview_feedback f JOIN interviews i ON i.id = f.interview_id "
                + "WHERE i.application_id = ? ORDER BY i.round_no";
        List<InterviewFeedback> feedbacks = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    feedbacks.add(map(rs));
                }
            }
        }
        return feedbacks;
    }

    public boolean existsByInterview(Connection connection, int interviewId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM interview_feedback WHERE interview_id = ?")) {
            ps.setInt(1, interviewId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private InterviewFeedback map(ResultSet rs) throws SQLException {
        return new InterviewFeedback(
                rs.getInt("id"),
                rs.getInt("interview_id"),
                rs.getInt("technical_rating"),
                rs.getInt("communication_rating"),
                rs.getInt("attitude_rating"),
                rs.getString("comments"),
                InterviewResult.valueOf(rs.getString("result")),
                rs.getString("created_by"),
                Jdbc.dateTime(rs, "created_at")
        );
    }
}
