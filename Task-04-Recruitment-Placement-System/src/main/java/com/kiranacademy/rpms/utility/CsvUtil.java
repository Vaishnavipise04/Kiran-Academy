package com.kiranacademy.rpms.utility;

import com.kiranacademy.rpms.exception.RPMSException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * File handling utility used by report export.
 */
public final class CsvUtil {

    private static final Path EXPORT_DIRECTORY = Path.of("exports");

    private CsvUtil() {
    }

    public static Path write(String fileName, List<String> headers, List<List<String>> rows)
            throws RPMSException {
        try {
            Files.createDirectories(EXPORT_DIRECTORY);
            Path target = EXPORT_DIRECTORY.resolve(fileName);
            try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
                writer.write(toCsvLine(headers));
                writer.newLine();
                for (List<String> row : rows) {
                    writer.write(toCsvLine(row));
                    writer.newLine();
                }
            }
            AppLogger.info("Report exported: " + target.toAbsolutePath());
            return target;
        } catch (IOException e) {
            throw new RPMSException("Unable to write CSV report: " + e.getMessage(), e);
        }
    }

    private static String toCsvLine(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(escape(values.get(i)));
        }
        return builder.toString();
    }

    private static String escape(String value) {
        String safe = value == null ? "" : value;
        boolean quote = safe.contains(",") || safe.contains("\"")
                || safe.contains("\n") || safe.contains("\r");
        String escaped = safe.replace("\"", "\"\"");
        return quote ? "\"" + escaped + "\"" : escaped;
    }
}
