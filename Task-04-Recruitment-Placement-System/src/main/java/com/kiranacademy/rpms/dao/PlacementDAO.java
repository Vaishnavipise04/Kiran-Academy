package com.kiranacademy.rpms.dao;

import com.kiranacademy.rpms.model.Placement;
import com.kiranacademy.rpms.model.PlacementStatus;
import com.kiranacademy.rpms.utility.Jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlacementDAO {

    private static final String SELECT = "SELECT p.id, p.candidate_id, p.company_id, p.job_id, "
            + "p.offer_id, p.placement_date, p.package_lpa, p.status, p.created_at, "
            + "CONCAT(c.first_name, ' ', c.last_name) AS candidate_name, "
            + "co.name AS company_name, j.title AS job_title "
            + "FROM placements p "
            + "JOIN candidates c ON c.id = p.candidate_id "
            + "JOIN companies co ON co.id = p.company_id "
            + "JOIN jobs j ON j.id = p.job_id ";

    public int insert(Connection connection, Placement placement) throws SQLException {
        String sql = "INSERT INTO placements (candidate_id, company_id, job_id, offer_id, "
                + "placement_date, package_lpa, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, placement.candidateId());
            ps.setInt(2, placement.companyId());
            ps.setInt(3, placement.jobId());
            ps.setInt(4, placement.offerId());
            Jdbc.setDate(ps, 5, placement.placementDate());
            ps.setBigDecimal(6, placement.packageLpa());
            ps.setString(7, placement.status().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Placement insert did not return a generated id");
    }

    public Optional<Placement> findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT + "WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Placement> findByCandidate(Connection connection, int candidateId)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                SELECT + "WHERE p.candidate_id = ?")) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Placement> findAll(Connection connection) throws SQLException {
        String sql = SELECT + "ORDER BY p.placement_date DESC, p.id DESC";
        List<Placement> placements = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                placements.add(map(rs));
            }
        }
        return placements;
    }

    public boolean existsByCandidate(Connection connection, int candidateId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM placements WHERE candidate_id = ?")) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void updateStatus(Connection connection, int placementId, PlacementStatus status)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE placements SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, placementId);
            ps.executeUpdate();
        }
    }

    public long count(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM placements");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private Placement map(ResultSet rs) throws SQLException {
        return new Placement(
                rs.getInt("id"),
                rs.getInt("candidate_id"),
                rs.getInt("company_id"),
                rs.getInt("job_id"),
                rs.getInt("offer_id"),
                Jdbc.date(rs, "placement_date"),
                rs.getBigDecimal("package_lpa"),
                PlacementStatus.valueOf(rs.getString("status")),
                Jdbc.dateTime(rs, "created_at"),
                rs.getString("candidate_name"),
                rs.getString("company_name"),
                rs.getString("job_title")
        );
    }
}
