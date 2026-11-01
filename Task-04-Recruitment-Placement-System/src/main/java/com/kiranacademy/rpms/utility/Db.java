package com.kiranacademy.rpms.utility;

import com.kiranacademy.rpms.exception.DataAccessException;
import com.kiranacademy.rpms.exception.RPMSException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Small transaction template. All service/database work goes through read() or
 * transaction() so connection handling, commits and rollbacks are consistent.
 */
public final class Db {

    @FunctionalInterface
    public interface Work<T> {
        T run(Connection connection) throws SQLException, RPMSException;
    }

    private Db() {
    }

    public static <T> T read(Work<T> work) throws RPMSException {
        try (Connection connection = DBConnection.getConnection()) {
            return work.run(connection);
        } catch (SQLException e) {
            AppLogger.error("Database read failed", e);
            throw new DataAccessException("Database read failed: " + e.getMessage(), e);
        }
    }

    public static <T> T transaction(Work<T> work) throws RPMSException {
        try (Connection connection = DBConnection.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException e) {
                rollback(connection, e);
                AppLogger.error("Transaction failed", e);
                throw new DataAccessException("Database transaction failed: " + e.getMessage(), e);
            } catch (RPMSException | RuntimeException e) {
                rollback(connection, e);
                throw e;
            } finally {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (SQLException ignored) {
                    // Connection is about to be closed.
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Unable to open database connection", e);
            throw new DataAccessException("Unable to connect to database: " + e.getMessage(), e);
        }
    }

    private static void rollback(Connection connection, Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            cause.addSuppressed(rollbackFailure);
            AppLogger.error("Rollback failed", rollbackFailure);
        }
    }
}
