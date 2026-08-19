package com.lucaf.robotic_core.stepperOnline.iDmRs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FaultCodeTest {

    @Test
    void testFromAlarmNone() {
        assertEquals(FaultCode.NONE, FaultCode.fromAlarm(0));
    }

    @Test
    void testFromAlarmKnownCodes() {
        assertEquals(FaultCode.OVER_CURRENT, FaultCode.fromAlarm(0x01));
        assertEquals(FaultCode.OVER_VOLTAGE, FaultCode.fromAlarm(0x02));
        assertEquals(FaultCode.CURRENT_SAMPLING_FAULT, FaultCode.fromAlarm(0x40));
        assertEquals(FaultCode.FAILED_TO_LOCK_SHAFT, FaultCode.fromAlarm(0x80));
        assertEquals(FaultCode.AUTO_TUNING_FAULT, FaultCode.fromAlarm(0x100));
        assertEquals(FaultCode.EEPROM_FAULT, FaultCode.fromAlarm(0x200));
    }

    @Test
    void testFromAlarmUnknown() {
        assertEquals(FaultCode.UNKNOWN, FaultCode.fromAlarm(0x400));
    }

    @Test
    void testFromAlarmFirstMatch() {
        // When multiple bits are set, the first matching code (by bit value) wins.
        assertEquals(FaultCode.OVER_CURRENT, FaultCode.fromAlarm(0x01 | 0x200));
    }

    @Test
    void testErrorFlags() {
        ErrorFlags noError = new ErrorFlags(0);
        assertFalse(noError.hasError());
        assertEquals(FaultCode.NONE, noError.getFaultCode());
        assertEquals("None", noError.getErrorDescription());

        ErrorFlags overCurrent = new ErrorFlags(0x01);
        assertTrue(overCurrent.hasError());
        assertEquals(FaultCode.OVER_CURRENT, overCurrent.getFaultCode());
        assertEquals("Over-current", overCurrent.getErrorDescription());
    }
}
