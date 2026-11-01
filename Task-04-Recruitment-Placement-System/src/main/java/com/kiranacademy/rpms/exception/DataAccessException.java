package com.kiranacademy.rpms.exception;

/**
 * Wraps SQLExceptions so that the service and UI layers never depend on JDBC details.
 */
public class DataAccessException extends RPMSException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
