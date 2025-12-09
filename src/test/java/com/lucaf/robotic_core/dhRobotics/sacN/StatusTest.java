package com.lucaf.robotic_core.dhRobotics.sacN;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StatusTest {

    @Test
    public void testStatusCodeCombinations() {
        for (int code = 0; code <= 0xFFFF; code++) {
            Status status = new Status(code);

            assertEquals(((code >> 15) & 1) == 1, status.isEmergencyStopState());
            assertEquals(((code >> 14) & 1) == 1, status.isPowerSupplyState());
            assertEquals(((code >> 13) & 1) == 1, status.isThrust());
            assertEquals(((code >> 10) & 1) == 1, status.isEnabled());
            assertEquals(((code >> 9) & 1) == 1, status.isHasAlarm());
            assertEquals(((code >> 8) & 1) == 1, status.isInMotion());
            assertEquals(((code >> 6) & 1) == 1, status.isBackHome());
            assertEquals(((code >> 5) & 1) == 1, status.isInPlace());
        }
    }
}
