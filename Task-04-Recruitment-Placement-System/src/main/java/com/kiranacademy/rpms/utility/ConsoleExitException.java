package com.kiranacademy.rpms.utility;

/**
 * Thrown when standard input is closed (for example Ctrl+Z on Windows), allowing
 * the console application to shut down cleanly.
 */
public class ConsoleExitException extends RuntimeException {

    public ConsoleExitException() {
        super("Console input closed");
    }
}
