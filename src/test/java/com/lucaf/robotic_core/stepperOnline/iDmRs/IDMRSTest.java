package com.lucaf.robotic_core.stepperOnline.iDmRs;

import com.lucaf.robotic_core.dataInterfaces.impl.Register;
import com.lucaf.robotic_core.mock.dataInterface.MockedRegisterInterface;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static com.lucaf.robotic_core.dhRobotics.rgi100.Constants.ABSOLUTE_ROTATION;
import static org.junit.jupiter.api.Assertions.*;

import static com.lucaf.robotic_core.stepperOnline.iDmRs.Constants.TARGET_POSITION_HIGH;


public class IDMRSTest {
    final HashMap<String, Object> state = new HashMap<>();
    final MockedRegisterInterface connection = new MockedRegisterInterface(1, "SAC_N Test Motor");

    final IDMRS motor = new IDMRS(
            connection,
            state
    );

    void initializeMotor() throws Exception {
        //
    }

    @Test
    void testLongPosition() throws Exception {
        initializeMotor();
        motor.setPosition(200000);
        assertEquals(200000, connection.readSignedLong(TARGET_POSITION_HIGH, false));
        Register[] regs = connection.readMultipleRegisters(TARGET_POSITION_HIGH[0] << 8 | TARGET_POSITION_HIGH[1], 2);
        int high = regs[0].getValue();
        int low = regs[1].toShort();
        System.out.println("High: " + String.format("0x%04X", high) + " Low: " + String.format("0x%04X", low));
        assertEquals(0x0003, high);
        assertEquals(0x0D40, low);
    }
}
