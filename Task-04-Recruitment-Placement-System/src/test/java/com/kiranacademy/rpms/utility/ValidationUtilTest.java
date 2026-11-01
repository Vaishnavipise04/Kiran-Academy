package com.kiranacademy.rpms.utility;

import com.kiranacademy.rpms.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilTest {

    @Test
    void validEmailAndPhonePass() {
        assertDoesNotThrow(() -> ValidationUtil.email("candidate@example.com"));
        assertDoesNotThrow(() -> ValidationUtil.phone("+91 98765-43210"));
    }

    @Test
    void invalidEmailAndPhoneFail() {
        assertThrows(ValidationException.class, () -> ValidationUtil.email("not-an-email"));
        assertThrows(ValidationException.class, () -> ValidationUtil.phone("abc"));
    }

    @Test
    void percentageMustStayWithinRange() {
        assertDoesNotThrow(() -> ValidationUtil.percentage(new BigDecimal("75.50")));
        assertThrows(ValidationException.class,
                () -> ValidationUtil.percentage(new BigDecimal("101")));
        assertThrows(ValidationException.class,
                () -> ValidationUtil.percentage(new BigDecimal("-1")));
    }

    @Test
    void ratingsMustBeOneToFive() {
        assertDoesNotThrow(() -> ValidationUtil.rating(5, "Rating"));
        assertThrows(ValidationException.class, () -> ValidationUtil.rating(0, "Rating"));
        assertThrows(ValidationException.class, () -> ValidationUtil.rating(6, "Rating"));
    }
}
