package com.kiranacademy.rpms.db;

import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.utility.AppLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes simple MySQL DDL/DML scripts bundled in the jar. The scripts contain
 * only plain statements (no stored procedures or DELIMITER blocks).
 */
public final class SqlScriptRunner {

    private SqlScriptRunner() {
    }

    public static int run(Connection connection, String resourcePath) throws RPMSException {
        List<String> statements = loadStatements(resourcePath);
        int executed = 0;
        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
                executed++;
            }
        } catch (SQLException e) {
            throw new RPMSException("Failed while executing " + resourcePath + ": " + e.getMessage(), e);
        }
        AppLogger.info("Executed " + executed + " statements from " + resourcePath);
        return executed;
    }

    static List<String> loadStatements(String resourcePath) throws RPMSException {
        String script;
        try (InputStream input = SqlScriptRunner.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new RPMSException("SQL script not found on classpath: " + resourcePath);
            }
            script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RPMSException("Unable to read SQL script " + resourcePath, e);
        }

        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String rawLine : script.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("--") || line.startsWith("#")) {
                continue;
            }
            current.append(line).append('\n');
            if (line.endsWith(";")) {
                String sql = current.toString().trim();
                sql = sql.substring(0, sql.length() - 1).trim();
                if (!sql.isEmpty()) {
                    statements.add(sql);
                }
                current.setLength(0);
            }
        }
        if (!current.toString().isBlank()) {
            statements.add(current.toString().trim());
        }
        return statements;
    }
}
