package com.kiranacademy.rpms.db;

import com.kiranacademy.rpms.config.AppConfig;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.utility.AppLogger;
import com.kiranacademy.rpms.utility.DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the database and loads the bundled schema/sample data.
 */
public final class DatabaseInitializer {

    public void initialize(boolean withSampleData) throws RPMSException {
        String databaseName = DBConnection.databaseName();
        if (!databaseName.matches("[A-Za-z0-9_]+")) {
            throw new RPMSException("Invalid database name in db.url: " + databaseName);
        }

        try (Connection connection = DriverManager.getConnection(
                DBConnection.serverUrl(), AppConfig.dbUser(), AppConfig.dbPassword())) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE IF NOT EXISTS `" + databaseName + "` "
                        + "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }
            SqlScriptRunner.run(connection, "/database/schema.sql");
            if (withSampleData) {
                SqlScriptRunner.run(connection, "/database/sample_data.sql");
            }
        } catch (SQLException e) {
            throw new RPMSException("Unable to initialise database. Check MySQL is running and "
                    + "the credentials in db.properties are correct. Details: " + e.getMessage(), e);
        }
        AppLogger.info("Database initialisation completed (sampleData=" + withSampleData + ")");
    }

    public boolean isSchemaReady() throws RPMSException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_name = 'users'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RPMSException("Unable to inspect database schema: " + e.getMessage(), e);
        }
    }
}
