package com.lucaf.robotic_core.dhRobotics.sacN;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Control {

    /**
     * Rising edge: perform action
     */
    private boolean action = false;

    /**
     * Rising edge: execution set to zero
     */
    private boolean toZero = false;

    /**
     * False: normal, True: pause
     */
    private boolean suspend = false;

    /**
     * Rising edge: reset
     */
    private boolean reset = false;

    /**
     * False: disable, True: enable
     */
    private boolean enable = false;

    /**
     * False: position, True: Push and pressure
     */
    private boolean thurst = false;

    /**
     * False: forward, True: reverse
     */
    private boolean direction = false;

    /**
     * False: absolute, True: relative
     */
    private boolean relative = false;

    /**
     * False: normal, True: lock
     */
    private boolean lock = false;

    /**
     * Get the control byte
     *
     * @return the control byte
     */
    public byte[] getByte() {
        StringBuilder sb = new StringBuilder();
        sb.append(action ? "1" : "0");
        sb.append(toZero ? "1" : "0");
        sb.append("0");
        sb.append(suspend ? "1" : "0");
        sb.append(reset ? "1" : "0");
        sb.append(enable ? "1" : "0");
        sb.append("000000");
        sb.append(thurst ? "1" : "0");
        sb.append(direction ? "1" : "0");
        sb.append(relative ? "1" : "0");
        sb.append(lock ? "1" : "0");
        sb.reverse();
        byte[] bytes = new byte[2];
        bytes[0] = (byte) Integer.parseInt(sb.substring(0, 8), 2);
        bytes[1] = (byte) Integer.parseInt(sb.substring(8), 2);
        return bytes;
    }

    public Control() {
    }

    public Control(byte high, byte low) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%8s", Integer.toBinaryString(high & 0xFF)).replace(' ', '0'));
        sb.append(String.format("%8s", Integer.toBinaryString(low & 0xFF)).replace(' ', '0'));
        action = sb.charAt(0) == '1';
        toZero = sb.charAt(1) == '1';
        suspend = sb.charAt(3) == '1';
        reset = sb.charAt(4) == '1';
        enable = sb.charAt(5) == '1';
        thurst = sb.charAt(8) == '1';
        direction = sb.charAt(9) == '1';
        relative = sb.charAt(10) == '1';
        lock = sb.charAt(11) == '1';
    }
}