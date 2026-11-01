package com.kiranacademy.rpms.utility;

import com.kiranacademy.rpms.exception.ValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtil {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateUtil() {
    }

    public static LocalDate parseDate(String value) throws ValidationException {
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date '" + value + "'. Expected format: yyyy-MM-dd");
        }
    }

    public static LocalDateTime parseDateTime(String value) throws ValidationException {
        String trimmed = value.trim();
        try {
            if (trimmed.length() == 10) {
                return LocalDate.parse(trimmed, DATE_FORMAT).atTime(10, 0);
            }
            return LocalDateTime.parse(trimmed, DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    "Invalid date/time '" + value + "'. Expected format: yyyy-MM-dd HH:mm");
        }
    }

    public static String format(LocalDate date) {
        return date == null ? "-" : date.format(DATE_FORMAT);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_TIME_FORMAT);
    }
}
