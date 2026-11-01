package com.kiranacademy.rpms.dao;

import com.kiranacademy.rpms.model.Candidate;
import com.kiranacademy.rpms.model.CandidateStatus;
import com.kiranacademy.rpms.utility.Jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CandidateDAO {

    private static final String COLUMNS = "id, registration_no, first_name, last_name, email, phone, "
            + "qualification, passing_year, percentage, technology, backlogs, skills, status, created_at";

    public int insert(Connection connection, Candidate candidate) throws SQLException {
        String sql = "INSERT INTO candidates (registration_no, first_name, last_name, email, phone, "
                + "qualification, passing_year, percentage, technology, backlogs, skills, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, candidate.registrationNo());
            ps.setString(2, candidate.firstName());
            ps.setString(3, candidate.lastName());
            ps.setString(4, candidate.email());
            ps.setString(5, candidate.phone());
            ps.setString(6, candidate.qualification());
            ps.setInt(7, candidate.passingYear());
            ps.setBigDecimal(8, candidate.percentage());
            ps.setString(9, candidate.technology());
            ps.setInt(10, candidate.backlogs());
            ps.setString(11, candidate.skills());
            ps.setString(12, candidate.status().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Candidate insert did not return a generated id");
    }

    public void update(Connection connection, Candidate candidate) throws SQLException {
        String sql = "UPDATE candidates SET first_name = ?, last_name = ?, email = ?, phone = ?, "
                + "qualification = ?, passing_year = ?, percentage = ?, technology = ?, backlogs = ?, "
                + "skills = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, candidate.firstName());
            ps.setString(2, candidate.lastName());
            ps.setString(3, candidate.email());
            ps.setString(4, candidate.phone());
            ps.setString(5, candidate.qualification());
            ps.setInt(6, candidate.passingYear());
            ps.setBigDecimal(7, candidate.percentage());
            ps.setString(8, candidate.technology());
            ps.setInt(9, candidate.backlogs());
            ps.setString(10, candidate.skills());
            ps.setString(11, candidate.status().name());
            ps.setInt(12, candidate.id());
            ps.executeUpdate();
        }
    }

    public Optional<Candidate> findById(Connection connection, int id) throws SQLException {
        return findOne(connection, "SELECT " + COLUMNS + " FROM candidates WHERE id = ?", id);
    }

    public Optional<Candidate> findByEmail(Connection connection, String email) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM candidates WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Candidate> findByRegistrationNo(Connection connection, String registrationNo)
            throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM candidates WHERE registration_no = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, registrationNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Candidate> findAll(Connection connection) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM candidates ORDER BY id";
        List<Candidate> candidates = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                candidates.add(map(rs));
            }
        }
        return candidates;
    }

    public List<Candidate> search(Connection connection, String keyword) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM candidates "
                + "WHERE LOWER(CONCAT(first_name, ' ', last_name, ' ', email, ' ', technology, ' ', "
                + "COALESCE(skills, ''))) LIKE ? ORDER BY id";
        List<Candidate> candidates = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    candidates.add(map(rs));
                }
            }
        }
        return candidates;
    }

    public boolean existsByEmail(Connection connection, String email) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM candidates WHERE LOWER(email) = LOWER(?)")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existsByPhone(Connection connection, String phone) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM candidates WHERE phone = ?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void updateStatus(Connection connection, int candidateId, CandidateStatus status)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE candidates SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, candidateId);
            ps.executeUpdate();
        }
    }

    public long count(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM candidates");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public long countByStatus(Connection connection, CandidateStatus status) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM candidates WHERE status = ?")) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public int nextSequence(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COALESCE(MAX(id), 0) + 1 FROM candidates");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Optional<Candidate> findOne(Connection connection, String sql, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    private Candidate map(ResultSet rs) throws SQLException {
        return new Candidate(
                rs.getInt("id"),
                rs.getString("registration_no"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("qualification"),
                rs.getInt("passing_year"),
                rs.getBigDecimal("percentage"),
                rs.getString("technology"),
                rs.getInt("backlogs"),
                rs.getString("skills"),
                CandidateStatus.valueOf(rs.getString("status")),
                Jdbc.dateTime(rs, "created_at")
        );
    }
}
