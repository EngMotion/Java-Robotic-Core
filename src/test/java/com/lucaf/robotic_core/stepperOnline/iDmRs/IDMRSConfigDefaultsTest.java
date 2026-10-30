package com.lucaf.robotic_core.stepperOnline.iDmRs;

import com.lucaf.robotic_core.SerialParams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class IDMRSConfigDefaultsTest {
    @Test
    void motorAndScaleHaveSeparateSerialDefaults() {
        IDMRSConfig motorConfig = new IDMRSConfig();
        SerialParams motor = motorConfig.getConnection();
        SerialParams scale = SerialParams.scaleDefaults();

        assertEquals("/dev/ttyUSB-porta0", motor.getComPort());
        assertEquals(19200, motor.getBaudrate());
        assertEquals("/dev/ttyUSB-porta1", scale.getComPort());
        assertEquals(9600, scale.getBaudrate());
        assertNotSame(motor, scale);
    }
}
