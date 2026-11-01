package com.kiranacademy.rpms.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashVerifiesCorrectPassword() {
        String stored = PasswordUtil.hash("Admin@123");
        assertTrue(PasswordUtil.verify("Admin@123", stored));
    }

    @Test
    void hashRejectsWrongPassword() {
        String stored = PasswordUtil.hash("Admin@123");
        assertFalse(PasswordUtil.verify("admin@123", stored));
        assertFalse(PasswordUtil.verify("", stored));
        assertFalse(PasswordUtil.verify(null, stored));
    }

    @Test
    void saltMakesHashesDifferent() {
        String first = PasswordUtil.hash("same-password");
        String second = PasswordUtil.hash("same-password");
        assertNotEquals(first, second);
        assertTrue(PasswordUtil.verify("same-password", first));
        assertTrue(PasswordUtil.verify("same-password", second));
    }

    @Test
    void malformedHashIsRejected() {
        assertFalse(PasswordUtil.verify("password", "not-a-valid-hash"));
    }
}
