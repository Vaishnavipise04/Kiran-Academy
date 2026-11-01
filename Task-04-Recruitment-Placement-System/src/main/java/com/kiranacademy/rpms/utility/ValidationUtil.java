package com.kiranacademy.rpms.utility;

import com.kiranacademy.rpms.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE =
            Pattern.compile("^[0-9+\\- ]{7,15}$");

    private ValidationUtil() {
    }

    public static String requiredText(String value, String field, int min, int max)
            throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            throw new ValidationException(field + " must be between " + min + " and " + max + " characters");
        }
        return trimmed;
    }

    public static String optionalText(String value, String field, int max) throws ValidationException {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new ValidationException(field + " must not exceed " + max + " characters");
        }
        return trimmed;
    }

    public static void email(String value) throws ValidationException {
        String email = requiredText(value, "Email", 5, 120);
        if (!EMAIL.matcher(email).matches()) {
            throw new ValidationException("Invalid email address: " + email);
        }
    }

    public static void phone(String value) throws ValidationException {
        String phone = requiredText(value, "Phone", 7, 20);
        if (!PHONE.matcher(phone).matches()) {
            throw new ValidationException("Invalid phone number. Use 7-15 digits, +, - or spaces");
        }
    }

    public static void percentage(BigDecimal value) throws ValidationException {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0
                || value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ValidationException("Percentage must be between 0 and 100");
        }
    }

    public static void passingYear(int year) throws ValidationException {
        int max = Year.now().getValue() + 1;
        if (year < 1980 || year > max) {
            throw new ValidationException("Passing year must be between 1980 and " + max);
        }
    }

    public static void nonNegative(int value, String field) throws ValidationException {
        if (value < 0) {
            throw new ValidationException(field + " cannot be negative");
        }
    }

    public static void positive(int value, String field) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(field + " must be greater than zero");
        }
    }

    public static void positive(BigDecimal value, String field) throws ValidationException {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(field + " must be greater than zero");
        }
    }

    public static void rating(int value, String field) throws ValidationException {
        if (value < 1 || value > 5) {
            throw new ValidationException(field + " must be between 1 and 5");
        }
    }

    public static void notFuture(LocalDate date, String field) throws ValidationException {
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new ValidationException(field + " cannot be in the future");
        }
    }
}
