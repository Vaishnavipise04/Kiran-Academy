package com.kiranacademy.rpms.dao;

import com.kiranacademy.rpms.model.Offer;
import com.kiranacademy.rpms.model.OfferStatus;
import com.kiranacademy.rpms.utility.Jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OfferDAO {

    private static final String SELECT = "SELECT o.id, o.application_id, o.offered_ctc, o.offer_date, "
            + "o.status, o.joining_date, o.created_at, "
            + "CONCAT(c.first_name, ' ', c.last_name) AS candidate_name, "
            + "j.title AS job_title, co.name AS company_name "
            + "FROM offers o "
            + "JOIN applications a ON a.id = o.application_id "
            + "JOIN candidates c ON c.id = a.candidate_id "
            + "JOIN jobs j ON j.id = a.job_id "
            + "JOIN companies co ON co.id = j.company_id ";

    public int insert(Connection connection, Offer offer) throws SQLException {
        String sql = "INSERT INTO offers (application_id, offered_ctc, offer_date, status, joining_date) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, offer.applicationId());
            ps.setBigDecimal(2, offer.offeredCtc());
            Jdbc.setDate(ps, 3, offer.offerDate());
            ps.setString(4, offer.status().name());
            Jdbc.setDate(ps, 5, offer.joiningDate());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Offer insert did not return a generated id");
    }

    public Optional<Offer> findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT + "WHERE o.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Offer> findByApplication(Connection connection, int applicationId)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                SELECT + "WHERE o.application_id = ?")) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Offer> findAll(Connection connection) throws SQLException {
        return findMany(connection, SELECT + "ORDER BY o.id DESC");
    }

    public List<Offer> findByCandidate(Connection connection, int candidateId) throws SQLException {
        return findMany(connection, SELECT + "WHERE a.candidate_id = ? ORDER BY o.id DESC", candidateId);
    }

    public void updateStatus(Connection connection, int offerId, OfferStatus status) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE offers SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, offerId);
            ps.executeUpdate();
        }
    }

    public void updateJoiningDate(Connection connection, int offerId, LocalDate joiningDate)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE offers SET joining_date = ? WHERE id = ?")) {
            Jdbc.setDate(ps, 1, joiningDate);
            ps.setInt(2, offerId);
            ps.executeUpdate();
        }
    }

    public long countByStatus(Connection connection, OfferStatus status) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM offers WHERE status = ?")) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public long count(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM offers");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private List<Offer> findMany(Connection connection, String sql, Integer... params)
            throws SQLException {
        List<Offer> offers = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    offers.add(map(rs));
                }
            }
        }
        return offers;
    }

    private Offer map(ResultSet rs) throws SQLException {
        return new Offer(
                rs.getInt("id"),
                rs.getInt("application_id"),
                rs.getBigDecimal("offered_ctc"),
                Jdbc.date(rs, "offer_date"),
                OfferStatus.valueOf(rs.getString("status")),
                Jdbc.date(rs, "joining_date"),
                Jdbc.dateTime(rs, "created_at"),
                rs.getString("candidate_name"),
                rs.getString("job_title"),
                rs.getString("company_name")
        );
    }
}
