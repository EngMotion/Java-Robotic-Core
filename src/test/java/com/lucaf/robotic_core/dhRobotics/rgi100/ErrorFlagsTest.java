package com.lucaf.robotic_core.dhRobotics.rgi100;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ErrorFlagsTest {

    @Test
    public void testErrorFlags() {
        ErrorFlags error1 = new ErrorFlags(0);
        assert(!error1.hasError());

        ErrorFlags error2 = new ErrorFlags(4);
        assertTrue(error2.hasError());
        assertEquals("Overheat", error2.getErrorDescription());

        ErrorFlags error3 = new ErrorFlags(8);
        assertTrue(error3.hasError());
        assertEquals("Overload", error3.getErrorDescription());

        ErrorFlags error4 = new ErrorFlags(11);
        assertTrue(error4.hasError());
        assertEquals("Overspeed", error4.getErrorDescription());

        ErrorFlags error5 = new ErrorFlags(99);
        assertTrue(error5.hasError());
        assertEquals("Unknown error: 99", error5.getErrorDescription());

    }
}
