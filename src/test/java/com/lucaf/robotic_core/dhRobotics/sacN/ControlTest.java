package com.lucaf.robotic_core.dhRobotics.sacN;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ControlTest {

    @Test
    public void testDefaultConstructor() {
        Control control = new Control();
        assertFalse(control.isAction());
        assertFalse(control.isToZero());
        assertFalse(control.isSuspend());
        assertFalse(control.isReset());
        assertFalse(control.isEnable());
        assertFalse(control.isThurst());
        assertFalse(control.isDirection());
        assertFalse(control.isRelative());
        assertFalse(control.isLock());
    }

    @Test
    public void testParameterizedConstructor() {
        byte high = (byte) 0b10101010;
        byte low = (byte) 0b01010101;
        Control control = new Control(high, low);

        assertTrue(control.isAction());
        assertFalse(control.isToZero());
        assertFalse(control.isSuspend());
        assertTrue(control.isReset());
        assertFalse(control.isEnable());
        assertFalse(control.isThurst());
        assertTrue(control.isDirection());
        assertFalse(control.isRelative());
        assertTrue(control.isLock());
    }

    @Test
    public void testGetByte() {
        Control control = new Control();
        control.setAction(true);
        control.setToZero(true);
        control.setSuspend(true);
        control.setReset(true);
        control.setEnable(true);
        control.setThurst(true);
        control.setDirection(true);
        control.setRelative(true);
        control.setLock(true);

        byte[] bytes = control.getByte();
        assertEquals((byte) 0b11011100, bytes[0]);
        assertEquals((byte) 0b11110000, bytes[1]);
    }

    @Test
    public void testSettersAndGetters() {
        Control control = new Control();

        control.setAction(true);
        assertTrue(control.isAction());
        control.setToZero(true);
        assertTrue(control.isToZero());
        control.setSuspend(true);
        assertTrue(control.isSuspend());
        control.setReset(true);
        assertTrue(control.isReset());
        control.setEnable(true);
        assertTrue(control.isEnable());
        control.setThurst(true);
        assertTrue(control.isThurst());
        control.setDirection(true);
        assertTrue(control.isDirection());
        control.setRelative(true);
        assertTrue(control.isRelative());
        control.setLock(true);
        assertTrue(control.isLock());
    }
}
