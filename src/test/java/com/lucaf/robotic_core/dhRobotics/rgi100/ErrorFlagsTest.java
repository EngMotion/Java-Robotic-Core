package com.lucaf.robotic_core.dhRobotics.rgi100;

import org.junit.jupiter.api.Test;

public class ErrorFlagsTest {

    @Test
    public void testErrorFlags() {
        ErrorFlags error1 = new ErrorFlags(0);
        assert(!error1.hasError());

        ErrorFlags error2 = new ErrorFlags(4);
        assert(error2.hasError());
        assert(error2.getErrorDescription().equals("Overheat"));

        ErrorFlags error3 = new ErrorFlags(8);
        assert(error3.hasError());
        assert(error3.getErrorDescription().equals("Overload"));

        ErrorFlags error4 = new ErrorFlags(11);
        assert(error4.hasError());
        assert(error4.getErrorDescription().equals("Overspeed"));

        ErrorFlags error5 = new ErrorFlags(99);
        assert(error5.hasError());
        assert(error5.getErrorDescription().equals("Unknown error: 99"));
    }
}
