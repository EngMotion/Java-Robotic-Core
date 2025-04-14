package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

import lombok.Getter;
import lombok.Setter;

/**
 * Class that represents the digital outputs of the motor.
 */
@Getter
@Setter
public class DigitalOutputs {

    /**
     * BIT 0: DO1
     */
    boolean DO1 = false;

    /**
     * BIT 1: DO2
     */
    boolean DO2 = false;

    /**
     * BIT 2: DO3
     */
    boolean DO3 = false;

    /**
     * Constructor of the DigitalOutputs class.
     * @param data the register data
     */
    public DigitalOutputs(int data){
        DO1 = (data & 0x01) != 0;
        DO2 = (data & 0x02) != 0;
        DO3 = (data & 0x04) != 0;
    }

    /**
     * Converts the digital outputs to an integer.
     * @return the integer representation of the digital outputs
     */
    public int toInt(){
        int data = 0;
        data = DO1 ? data | 0x01 : data;
        data = DO2 ? data | 0x02 : data;
        data = DO3 ? data | 0x04 : data;
        return data;
    }

    @Override
    public String toString() {
        return "DigitalOutputs{" +
                "DO1=" + DO1 +
                ", DO2=" + DO2 +
                ", DO3=" + DO3 +
                '}';
    }
}
