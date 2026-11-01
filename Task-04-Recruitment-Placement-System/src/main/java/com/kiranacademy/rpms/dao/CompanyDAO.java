package com.kiranacademy.rpms.dao;

import com.kiranacademy.rpms.model.Company;
import com.kiranacademy.rpms.model.CompanyStatus;
import com.kiranacademy.rpms.utility.Jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompanyDAO {

    private static final String COLUMNS =
            "id, name, industry, location, website, contact_person, contact_email, status, created_at";

    public int insert(Connection connection, Company company) throws SQLException {
        String sql = "INSERT INTO companies (name, industry, location, website, contact_person, "
                + "contact_email, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, company.name());
            ps.setString(2, company.industry());
            ps.setString(3, company.location());
            ps.setString(4, company.website());
            ps.setString(5, company.contactPerson());
            ps.setString(6, company.contactEmail());
            ps.setString(7, company.status().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Company insert did not return a generated id");
    }

    public void update(Connection connection, Company company) throws SQLException {
        String sql = "UPDATE companies SET name = ?, industry = ?, location = ?, website = ?, "
                + "contact_person = ?, contact_email = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, company.name());
            ps.setString(2, company.industry());
            ps.setString(3, company.location());
            ps.setString(4, company.website());
            ps.setString(5, company.contactPerson());
            ps.setString(6, company.contactEmail());
            ps.setString(7, company.status().name());
            ps.setInt(8, company.id());
            ps.executeUpdate();
        }
    }

    public Optional<Company> findById(Connection connection, int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM companies WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Company> findByName(Connection connection, String name) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM companies WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Company> findAll(Connection connection) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM companies ORDER BY name";
        List<Company> companies = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                companies.add(map(rs));
            }
        }
        return companies;
    }

    public void updateStatus(Connection connection, int companyId, CompanyStatus status)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE companies SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, companyId);
            ps.executeUpdate();
        }
    }

    public long count(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM companies");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public long countActive(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM companies WHERE status = 'ACTIVE'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private Company map(ResultSet rs) throws SQLException {
        return new Company(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("industry"),
                rs.getString("location"),
                rs.getString("website"),
                rs.getString("contact_person"),
                rs.getString("contact_email"),
                CompanyStatus.valueOf(rs.getString("status")),
                Jdbc.dateTime(rs, "created_at")
        );
    }
}
