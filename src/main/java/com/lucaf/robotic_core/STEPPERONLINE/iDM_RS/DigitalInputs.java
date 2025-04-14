package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

import lombok.Getter;

/**
 * Class that represents the digital inputs of the motor.
 */
@Getter
public class DigitalInputs {
    /**
     * BIT0: DI1
     */
    boolean DI1 = false;

    /**
     * BIT1: DI2
     */
    boolean DI2 = false;

    /**
     * BIT2: DI3
     */
    boolean DI3 = false;
    /**
     * BIT3: DI4
     */
    boolean DI4 = false;
    /**
     * BIT4: DI5
     */
    boolean DI5 = false;
    /**
     * BIT5: DI6
     */
    boolean DI6 = false;
    /**
     * BIT6: DI7
     */
    boolean DI7 = false;

    /**
     * Constructor of the DigitalInputs class.
     * @param data the register data
     */
    public DigitalInputs(int data){
        DI1 = (data & 0x01) != 0;
        DI2 = (data & 0x02) != 0;
        DI3 = (data & 0x04) != 0;
        DI4 = (data & 0x08) != 0;
        DI5 = (data & 0x10) != 0;
        DI6 = (data & 0x20) != 0;
        DI7 = (data & 0x40) != 0;
    }

    /**
     * Converts the digital inputs to an integer.
     * @return the integer representation of the digital inputs
     */
    public int toInt(){
        int data = 0;
        data = DI1 ? data | 0x01 : data;
        data = DI2 ? data | 0x02 : data;
        data = DI3 ? data | 0x04 : data;
        data = DI4 ? data | 0x08 : data;
        data = DI5 ? data | 0x10 : data;
        data = DI6 ? data | 0x20 : data;
        data = DI7 ? data | 0x40 : data;
        return data;
    }

    @Override
    public String toString() {
        return "DigitalInputs{" +
                "DI1=" + DI1 +
                ", DI2=" + DI2 +
                ", DI3=" + DI3 +
                ", DI4=" + DI4 +
                ", DI5=" + DI5 +
                ", DI6=" + DI6 +
                ", DI7=" + DI7 +
                '}';
    }
}
