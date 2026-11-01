package com.kiranacademy.rpms;

import com.kiranacademy.rpms.config.ApplicationContext;
import com.kiranacademy.rpms.db.DatabaseInitializer;
import com.kiranacademy.rpms.exception.RPMSException;
import com.kiranacademy.rpms.ui.ConsoleApplication;
import com.kiranacademy.rpms.utility.ConsoleExitException;

import java.util.List;

public final class Main {

    public static void main(String[] args) {
        boolean init = false;
        boolean sample = true;
        for (String arg : args) {
            if ("--init-db".equalsIgnoreCase(arg) || "init".equalsIgnoreCase(arg)) {
                init = true;
            } else if ("--no-sample".equalsIgnoreCase(arg)) {
                sample = false;
            } else if ("--help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg)) {
                printHelp();
                return;
            }
        }

        try {
            if (init) {
                runInitialization(sample);
                return;
            }
            new ConsoleApplication(new ApplicationContext()).run();
        } catch (ConsoleExitException e) {
            System.out.println("\nGoodbye.");
        } catch (RPMSException e) {
            System.err.println("\n[ERROR] " + e.getMessage());
            System.err.println("Run with --init-db to prepare the database, or check db.properties.");
        } catch (Exception e) {
            System.err.println("\n[FATAL] " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runInitialization(boolean withSampleData) throws RPMSException {
        System.out.println("Initialising HireTrack database...");
        new DatabaseInitializer().initialize(withSampleData);
        ApplicationContext context = new ApplicationContext();
        List<String> messages = context.authService.bootstrapDefaultUsers();
        System.out.println("Database and schema are ready.");
        if (withSampleData) {
            System.out.println("Sample companies, candidates, jobs and pipeline data were loaded.");
        }
        if (messages.isEmpty()) {
            System.out.println("Default user accounts already exist.");
        } else {
            System.out.println("\nDefault accounts:");
            messages.forEach(message -> System.out.println("  " + message));
        }
        System.out.println("\nStart the application with: java -jar target/rpms.jar");
    }

    private static void printHelp() {
        System.out.println("""
                HireTrack - Recruitment & Placement Management System

                Usage:
                  java -jar target/rpms.jar               Start the interactive console
                  java -jar target/rpms.jar --init-db     Create database, tables and sample data
                  java -jar target/rpms.jar --init-db --no-sample
                                                          Create schema without sample data

                Database settings live in src/main/resources/db.properties and can be
                overridden with RPMS_DB_URL, RPMS_DB_USER and RPMS_DB_PASSWORD.
                """);
    }

    private Main() {
    }
}
