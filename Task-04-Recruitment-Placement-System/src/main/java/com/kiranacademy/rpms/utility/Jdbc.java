package com.kiranacademy.rpms.utility;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JDBC date/null mapping helpers shared by all DAOs.
 */
public final class Jdbc {

    private Jdbc() {
    }

    public static LocalDateTime dateTime(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public static LocalDate date(ResultSet rs, String column) throws SQLException {
        java.sql.Date date = rs.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    public static void setDateTime(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        ps.setTimestamp(index, value == null ? null : Timestamp.valueOf(value));
    }

    public static void setDate(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        ps.setDate(index, value == null ? null : java.sql.Date.valueOf(value));
    }

    public static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}
