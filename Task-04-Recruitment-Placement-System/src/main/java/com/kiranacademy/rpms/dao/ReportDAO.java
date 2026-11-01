package com.kiranacademy.rpms.dao;

import com.kiranacademy.rpms.utility.Jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only aggregate queries used by reports and dashboards.
 */
public class ReportDAO {

    public record StatusCount(String status, long count) {
    }

    public record JobStat(int jobId, String title, String company, long applications,
                          long shortlisted, long selected, long joined) {
    }

    public record CompanyStat(String company, long jobs, long applications, long offers,
                              long joined, BigDecimal averagePackage) {
    }

    public record PlacementStat(String candidate, String company, String job,
                                java.time.LocalDate placementDate, BigDecimal packageLpa,
                                String status) {
    }

    public List<StatusCount> applicationPipeline(Connection connection) throws SQLException {
        String sql = "SELECT status, COUNT(*) AS total FROM applications GROUP BY status "
                + "ORDER BY FIELD(status, 'APPLIED', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', "
                + "'INTERVIEWED', 'SELECTED', 'OFFERED', 'JOINED', 'ON_HOLD', 'REJECTED', 'WITHDRAWN')";
        List<StatusCount> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new StatusCount(rs.getString("status"), rs.getLong("total")));
            }
        }
        return result;
    }

    public List<JobStat> jobWiseStats(Connection connection) throws SQLException {
        String sql = "SELECT j.id, j.title, co.name AS company_name, COUNT(a.id) AS applications, "
                + "COALESCE(SUM(CASE WHEN a.status IN ('SHORTLISTED','INTERVIEW_SCHEDULED',"
                + "'INTERVIEWED','SELECTED','OFFERED','JOINED') THEN 1 ELSE 0 END), 0) AS shortlisted, "
                + "COALESCE(SUM(CASE WHEN a.status IN ('SELECTED','OFFERED','JOINED') "
                + "THEN 1 ELSE 0 END), 0) AS selected, "
                + "COALESCE(SUM(CASE WHEN a.status = 'JOINED' THEN 1 ELSE 0 END), 0) AS joined "
                + "FROM jobs j JOIN companies co ON co.id = j.company_id "
                + "LEFT JOIN applications a ON a.job_id = j.id "
                + "GROUP BY j.id, j.title, co.name ORDER BY applications DESC, j.id";
        List<JobStat> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new JobStat(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("company_name"),
                        rs.getLong("applications"),
                        rs.getLong("shortlisted"),
                        rs.getLong("selected"),
                        rs.getLong("joined")
                ));
            }
        }
        return result;
    }

    public List<CompanyStat> companyHiringStats(Connection connection) throws SQLException {
        String sql = "SELECT co.name, COUNT(DISTINCT j.id) AS jobs, "
                + "COUNT(DISTINCT a.id) AS applications, COUNT(DISTINCT o.id) AS offers, "
                + "COUNT(DISTINCT p.id) AS joined, COALESCE(AVG(p.package_lpa), 0) AS avg_package "
                + "FROM companies co "
                + "LEFT JOIN jobs j ON j.company_id = co.id "
                + "LEFT JOIN applications a ON a.job_id = j.id "
                + "LEFT JOIN offers o ON o.application_id = a.id "
                + "LEFT JOIN placements p ON p.company_id = co.id "
                + "GROUP BY co.id, co.name ORDER BY joined DESC, co.name";
        List<CompanyStat> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new CompanyStat(
                        rs.getString("name"),
                        rs.getLong("jobs"),
                        rs.getLong("applications"),
                        rs.getLong("offers"),
                        rs.getLong("joined"),
                        rs.getBigDecimal("avg_package")
                ));
            }
        }
        return result;
    }

    public List<PlacementStat> placementStats(Connection connection) throws SQLException {
        String sql = "SELECT CONCAT(c.first_name, ' ', c.last_name) AS candidate_name, "
                + "co.name AS company_name, j.title AS job_title, p.placement_date, "
                + "p.package_lpa, p.status FROM placements p "
                + "JOIN candidates c ON c.id = p.candidate_id "
                + "JOIN companies co ON co.id = p.company_id "
                + "JOIN jobs j ON j.id = p.job_id "
                + "ORDER BY p.placement_date DESC, p.id DESC";
        List<PlacementStat> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new PlacementStat(
                        rs.getString("candidate_name"),
                        rs.getString("company_name"),
                        rs.getString("job_title"),
                        Jdbc.date(rs, "placement_date"),
                        rs.getBigDecimal("package_lpa"),
                        rs.getString("status")
                ));
            }
        }
        return result;
    }
}
