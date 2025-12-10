package com.lucaf.robotic_core.stepperOnline.iDmRs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ControlMode class.
 */
public class ControlModeTest {

    @Test
    public void testControlMode() {
        for (int i = 0; i < 128; i++) {
            ControlMode controlMode = new ControlMode(i);
            System.out.println("Testing mode: " + i + " -> " + controlMode.toString());
            assertEquals(i, controlMode.toCode());
        }
    }

    @Test
    public void testPathMode() {
        ControlMode controlMode = new ControlMode();
        controlMode.setPATH_MODE(new boolean[]{true, false, true, false, true, false});
        int expectedCode = 0;
        expectedCode |= (1 << 8); // BIT 8
        expectedCode |= (0 << 9); // BIT 9
        expectedCode |= (1 << 10); // BIT 10
        expectedCode |= (0 << 11); // BIT 11
        expectedCode |= (1 << 12); // BIT 12
        expectedCode |= (0 << 13); // BIT 13
        assertEquals(expectedCode, controlMode.toCode());
    }
}
