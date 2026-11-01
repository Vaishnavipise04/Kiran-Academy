package com.kiranacademy.rpms.utility;

import com.kiranacademy.rpms.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Central JDBC connection factory. Services obtain connections through {@link Db}
 * so that transaction boundaries stay in one place.
 */
public final class DBConnection {

    static {
        try {
            Class.forName(AppConfig.dbDriver());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "MySQL JDBC driver not found. Run the project through Maven so the connector is available.");
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(AppConfig.dbUrl(), AppConfig.dbUser(), AppConfig.dbPassword());
    }

    public static void testConnection() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
        }
    }

    public static String serverUrl() {
        String url = AppConfig.dbUrl();
        int queryIndex = url.indexOf('?');
        String query = queryIndex >= 0 ? url.substring(queryIndex) : "";
        String base = queryIndex >= 0 ? url.substring(0, queryIndex) : url;
        int slash = base.lastIndexOf('/');
        String server = slash >= 0 ? base.substring(0, slash) : base;
        return server + "/" + query;
    }

    public static String databaseName() {
        String url = AppConfig.dbUrl();
        int queryIndex = url.indexOf('?');
        String base = queryIndex >= 0 ? url.substring(0, queryIndex) : url;
        int slash = base.lastIndexOf('/');
        return slash >= 0 ? base.substring(slash + 1) : "rpms";
    }
}
