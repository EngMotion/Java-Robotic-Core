package com.lucaf.robotic_core.KERN.K700;

import com.lucaf.robotic_core.impl.ScaleResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class K700Test {

    @Test
    void parseKeepsTheHundredsDigit() {
        ScaleResponse response = K700.parse(" 156.111  g   ");

        assertNotNull(response);
        assertEquals(156.111, response.getWeight(), 0.0001);
        assertEquals("g", response.getUnit());
    }
}