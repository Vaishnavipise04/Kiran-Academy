package com.kiranacademy.rpms.dao;

import com.kiranacademy.rpms.model.Job;
import com.kiranacademy.rpms.model.JobStatus;
import com.kiranacademy.rpms.utility.Jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JobDAO {

    private static final String SELECT = "SELECT j.id, j.company_id, c.name AS company_name, j.title, "
            + "j.technology, j.qualification_required, j.min_percentage, j.passing_year_from, "
            + "j.max_backlogs, j.vacancies, j.package_lpa, j.location, j.status, "
            + "j.application_deadline, j.created_at FROM jobs j "
            + "JOIN companies c ON c.id = j.company_id ";

    public int insert(Connection connection, Job job) throws SQLException {
        String sql = "INSERT INTO jobs (company_id, title, technology, qualification_required, "
                + "min_percentage, passing_year_from, max_backlogs, vacancies, package_lpa, location, "
                + "status, application_deadline) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, job.companyId());
            ps.setString(2, job.title());
            ps.setString(3, job.technology());
            ps.setString(4, job.qualificationRequired());
            ps.setBigDecimal(5, job.minPercentage());
            ps.setInt(6, job.passingYearFrom());
            ps.setInt(7, job.maxBacklogs());
            ps.setInt(8, job.vacancies());
            ps.setBigDecimal(9, job.packageLpa());
            ps.setString(10, job.location());
            ps.setString(11, job.status().name());
            Jdbc.setDate(ps, 12, job.applicationDeadline());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Job insert did not return a generated id");
    }

    public void update(Connection connection, Job job) throws SQLException {
        String sql = "UPDATE jobs SET title = ?, technology = ?, qualification_required = ?, "
                + "min_percentage = ?, passing_year_from = ?, max_backlogs = ?, vacancies = ?, "
                + "package_lpa = ?, location = ?, status = ?, application_deadline = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, job.title());
            ps.setString(2, job.technology());
            ps.setString(3, job.qualificationRequired());
            ps.setBigDecimal(4, job.minPercentage());
            ps.setInt(5, job.passingYearFrom());
            ps.setInt(6, job.maxBacklogs());
            ps.setInt(7, job.vacancies());
            ps.setBigDecimal(8, job.packageLpa());
            ps.setString(9, job.location());
            ps.setString(10, job.status().name());
            Jdbc.setDate(ps, 11, job.applicationDeadline());
            ps.setInt(12, job.id());
            ps.executeUpdate();
        }
    }

    public Optional<Job> findById(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT + "WHERE j.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Job> findAll(Connection connection, boolean openOnly) throws SQLException {
        return findMany(connection, SELECT + (openOnly ? "WHERE j.status = 'OPEN' " : "")
                + "ORDER BY j.created_at DESC, j.id DESC", null);
    }

    public List<Job> findByCompany(Connection connection, int companyId) throws SQLException {
        return findMany(connection, SELECT + "WHERE j.company_id = ? ORDER BY j.id", companyId);
    }

    public List<Job> searchByTechnology(Connection connection, String technology) throws SQLException {
        String sql = SELECT + "WHERE (LOWER(j.technology) LIKE ? OR LOWER(j.title) LIKE ?) "
                + "AND j.status = 'OPEN' ORDER BY j.id";
        return findMany(connection, sql, null, technology.toLowerCase(), technology.toLowerCase());
    }

    public void updateStatus(Connection connection, int jobId, JobStatus status) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE jobs SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, jobId);
            ps.executeUpdate();
        }
    }

    public long countOpen(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM jobs WHERE status = 'OPEN'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private List<Job> findMany(Connection connection, String sql, Integer integerParam,
                               String... stringParams) throws SQLException {
        List<Job> jobs = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            if (integerParam != null) {
                ps.setInt(index++, integerParam);
            }
            for (String value : stringParams) {
                ps.setString(index++, value);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jobs.add(map(rs));
                }
            }
        }
        return jobs;
    }

    private Job map(ResultSet rs) throws SQLException {
        return new Job(
                rs.getInt("id"),
                rs.getInt("company_id"),
                rs.getString("company_name"),
                rs.getString("title"),
                rs.getString("technology"),
                rs.getString("qualification_required"),
                rs.getBigDecimal("min_percentage"),
                rs.getInt("passing_year_from"),
                rs.getInt("max_backlogs"),
                rs.getInt("vacancies"),
                rs.getBigDecimal("package_lpa"),
                rs.getString("location"),
                JobStatus.valueOf(rs.getString("status")),
                Jdbc.date(rs, "application_deadline"),
                Jdbc.dateTime(rs, "created_at")
        );
    }
}
