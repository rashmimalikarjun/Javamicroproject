package com.abctelecom.nms.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashThenVerifyRoundTrip() {
        String stored = PasswordUtil.hash("Sup3rSecret");
        assertTrue(stored.startsWith("PBKDF2$"));
        assertTrue(PasswordUtil.verify("Sup3rSecret", stored));
        assertFalse(PasswordUtil.verify("WrongPass1", stored));
    }

    @Test
    void saltsDifferBetweenHashesOfSamePassword() {
        assertNotEquals(PasswordUtil.hash("Passw0rd"), PasswordUtil.hash("Passw0rd"));
    }

    @Test
    void verifyToleratesMalformedStoredValues() {
        assertFalse(PasswordUtil.verify("x", "not-a-hash"));
        assertFalse(PasswordUtil.verify("x", ""));
    }

    @Test
    void strengthPolicy() {
        assertFalse(PasswordUtil.isStrongEnough("short1"));     // < 8 chars
        assertFalse(PasswordUtil.isStrongEnough("allletters")); // no digit
        assertFalse(PasswordUtil.isStrongEnough(null));
        assertTrue(PasswordUtil.isStrongEnough("Str0ngPass"));
    }
}
