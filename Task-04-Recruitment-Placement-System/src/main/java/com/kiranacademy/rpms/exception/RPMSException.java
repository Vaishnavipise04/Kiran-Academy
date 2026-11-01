package com.kiranacademy.rpms.exception;

/**
 * Root checked exception for all recoverable business and data access failures.
 */
public class RPMSException extends Exception {

    public RPMSException(String message) {
        super(message);
    }

    public RPMSException(String message, Throwable cause) {
        super(message, cause);
    }
}
