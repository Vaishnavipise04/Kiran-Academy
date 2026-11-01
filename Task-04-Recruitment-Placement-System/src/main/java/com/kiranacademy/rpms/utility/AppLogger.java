package com.kiranacademy.rpms.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * File-based application logger. Logs are written to the logs directory and also
 * shown on the console for errors.
 */
public final class AppLogger {

    private static final Logger LOGGER = Logger.getLogger("rpms");

    static {
        try {
            Files.createDirectories(Path.of("logs"));
            FileHandler handler = new FileHandler("logs/rpms-%g.log", 1_000_000, 3, true);
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.INFO);
            LOGGER.addHandler(handler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Unable to initialise file logging: " + e.getMessage());
        }
    }

    private AppLogger() {
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warning(message);
    }

    public static void error(String message, Throwable error) {
        LOGGER.log(Level.SEVERE, message, error);
    }
}
