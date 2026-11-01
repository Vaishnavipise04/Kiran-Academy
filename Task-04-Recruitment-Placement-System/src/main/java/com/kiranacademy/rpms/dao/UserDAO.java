package com.kiranacademy.rpms.dao;

import com.kiranacademy.rpms.model.Role;
import com.kiranacademy.rpms.model.User;
import com.kiranacademy.rpms.utility.Jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    private static final String COLUMNS =
            "id, username, password_hash, full_name, role, candidate_id, active, created_at";

    public Optional<User> findByUsername(Connection connection, String username) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> findById(Connection connection, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<User> findAll(Connection connection) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
        }
        return users;
    }

    public long count(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public int insert(Connection connection, User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, candidate_id, active) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.username());
            ps.setString(2, user.passwordHash());
            ps.setString(3, user.fullName());
            ps.setString(4, user.role().name());
            ps.setObject(5, user.candidateId());
            ps.setBoolean(6, user.active());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("User insert did not return a generated id");
    }

    public void updatePassword(Connection connection, int userId, String passwordHash) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET password_hash = ? WHERE id = ?")) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void setActive(Connection connection, int userId, boolean active) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET active = ? WHERE id = ?")) {
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                Role.valueOf(rs.getString("role")),
                (Integer) rs.getObject("candidate_id"),
                rs.getBoolean("active"),
                Jdbc.dateTime(rs, "created_at")
        );
    }
}
