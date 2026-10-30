package com.lucaf.robotic_core.KERN.K700;

import com.lucaf.robotic_core.impl.ScaleResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class K700Test {

    @Test
    void parseKeepsTheHundredsDigit() {
        ScaleResponse response = K700.parse(" 156.111  g   ");

        assertNotNull(response);
        assertEquals(156.111, response.getWeight(), 0.0001);
        assertEquals("g", response.getUnit());
    }

    @Test
    void parseReadsTwoIntegerAndFiveDecimalDigitsBelowHundredGrams() {
        ScaleResponse response = K700.parse(" 12.34567 g   ");

        assertNotNull(response);
        assertFalse(response.isError());
        assertEquals(12.34567, response.getWeight(), 0.000001);
        assertEquals("g", response.getUnit());
        assertTrue(response.isStable());
    }

    @Test
    void parseReadsThreeIntegerAndFourDecimalDigitsAboveHundredGrams() {
        ScaleResponse response = K700.parse(" 156.1111 g   ");

        assertNotNull(response);
        assertEquals(156.1111, response.getWeight(), 0.00001);
    }

    @Test
    void parseDoesNotDependOnHowTheNumberIsPadded() {
        assertEquals(12.34567, K700.parse("  12.34567 g   ").getWeight(), 0.000001);
        assertEquals(12.34567, K700.parse(" 12.34567  g   ").getWeight(), 0.000001);
    }

    @Test
    void parseReadsNegativeWeight() {
        assertEquals(-12.34567, K700.parse("-12.34567 g   ").getWeight(), 0.000001);
    }

    @Test
    void parseTreatsMissingUnitAsUnstable() {
        ScaleResponse response = K700.parse(" 12.34567       ");

        assertNotNull(response);
        assertFalse(response.isStable());
    }

    @Test
    void parseReportsSystemErrorFrame() {
        assertTrue(K700.parse("   ERR 1 23   ").isError());
    }
}