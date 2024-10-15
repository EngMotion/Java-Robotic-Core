package com.lucaf.robotic_core.NANOTEC.PD4E_RTU;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class that represents the control word of the PD4-E-RTU
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InputSpecialFunction {
    /**
     * Bit 0: Negative Limit
     */
    private boolean negativeLimit = false;

    /**
     * Bit 1: Positive Limit
     */
    private boolean positiveLimit = false;

    /**
     * Bit 2: Home Switch
     */
    private boolean homeSwitch = false;

    /**
     * Bit 3: Interlock
     */
    private boolean interlock = false;

    /**
     * Constructor of the class
     * @param n the integer that represents the digital outputs
     */
    public InputSpecialFunction(int n){
        negativeLimit = (n & 1) == 1;
        positiveLimit = (n & (1 << 1)) == (1 << 1);
        homeSwitch = (n & (1 << 2)) == (1 << 2);
        interlock = (n & (1 << 3)) == (1 << 3);
    }

    /**
     * Method that returns the integer that represents the digital outputs
     * @return the integer that represents the digital outputs
     */
    public int toInt(){
        int n = 0;
        if(negativeLimit) n |= 1;
        if(positiveLimit) n |= (1 << 1);
        if(homeSwitch) n |= (1 << 2);
        if(interlock) n |= (1 << 3);
        return n;
    }

    /**
     * Method that returns the string representation of the class
     * @return the string representation of the class
     */
    @Override
    public String toString(){
        return "Negative Limit: " + negativeLimit + ", Positive Limit: " + positiveLimit + ", Home Switch: " + homeSwitch + ", Interlock: " + interlock;
    }
}
