package com.kiranacademy.rpms.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads database settings from db.properties and allows environment overrides.
 */
public final class AppConfig {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = AppConfig.class.getResourceAsStream("/db.properties")) {
            if (input == null) {
                throw new IllegalStateException("db.properties not found on the classpath");
            }
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Unable to load db.properties: " + e.getMessage());
        }
    }

    private AppConfig() {
    }

    public static String get(String key, String defaultValue) {
        String envKey = "RPMS_" + key.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static String get(String key) {
        String value = get(key, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing configuration value: " + key);
        }
        return value;
    }

    public static String dbUrl() {
        return get("db.url");
    }

    public static String dbUser() {
        return get("db.user");
    }

    public static String dbPassword() {
        return get("db.password", "");
    }

    public static String dbDriver() {
        return get("db.driver", "com.mysql.cj.jdbc.Driver");
    }
}
