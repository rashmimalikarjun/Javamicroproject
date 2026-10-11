package com.abctelecom.nms.service;

import com.abctelecom.nms.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorsTest {

    @Test
    void requiredRejectsBlankAndTrims() throws ValidationException {
        assertThrows(ValidationException.class, () -> Validators.required("   ", "name"));
        assertThrows(ValidationException.class, () -> Validators.required(null, "name"));
        assertEquals("abc", Validators.required(" abc ", "name"));
    }

    @Test
    void idEnforcesCharsetAndLength() throws ValidationException {
        assertEquals("BS-01_a", Validators.id("BS-01_a", "stationId"));
        assertThrows(ValidationException.class, () -> Validators.id("a", "stationId"));      // too short
        assertThrows(ValidationException.class, () -> Validators.id("bad id!", "stationId")); // space/!
    }

    @Test
    void rangeIntAndDoubleBoundChecks() throws ValidationException {
        assertEquals(42, Validators.rangeInt("42", "n", 0, 100));
        assertThrows(ValidationException.class, () -> Validators.rangeInt("101", "n", 0, 100));
        assertThrows(ValidationException.class, () -> Validators.rangeInt("x", "n", 0, 100));
        assertEquals(1.5, Validators.rangeDouble("1.5", "d", 0, 2), 1e-9);
        assertThrows(ValidationException.class, () -> Validators.rangeDouble("NaN", "d", 0, 2));
        assertThrows(ValidationException.class, () -> Validators.rangeDouble("Infinity", "d", 0, 2));
    }

    @Test
    void oneOfNormalizesCase() throws ValidationException {
        assertEquals("ON", Validators.oneOf("on", "power", Validators.POWER_STATUS));
        assertThrows(ValidationException.class, () -> Validators.oneOf("BOGUS", "power", Validators.POWER_STATUS));
    }

    @Test
    void loadMustNotExceedCapacity() throws ValidationException {
        Validators.loadWithinCapacity(50, 100); // must not throw
        assertThrows(ValidationException.class, () -> Validators.loadWithinCapacity(150, 100));
    }
}
