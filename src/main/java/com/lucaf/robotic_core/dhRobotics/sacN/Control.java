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
        int high = 0;
        int low = 0;

        // High byte
        high |= (action ? 1 : 0) << 7;
        high |= (toZero ? 1 : 0) << 6;
        // Bit 5 is unused (always 0)
        high |= (suspend ? 1 : 0) << 4;
        high |= (reset ? 1 : 0) << 3;
        high |= (enable ? 1 : 0) << 2;
        // Bits 1 and 0 are unused (always 0)

        // Low byte
        low |= (thurst ? 1 : 0) << 7;
        low |= (direction ? 1 : 0) << 6;
        low |= (relative ? 1 : 0) << 5;
        low |= (lock ? 1 : 0) << 4;
        // Bits 3-0 are unused (always 0)

        return new byte[] { (byte) high, (byte) low };
    }

    public Control() {
    }

    public Control(byte high, byte low) {
        action    = ((high >> 7) & 0x01) == 1;
        toZero    = ((high >> 6) & 0x01) == 1;
        suspend   = ((high >> 4) & 0x01) == 1;
        reset     = ((high >> 3) & 0x01) == 1;
        enable    = ((high >> 2) & 0x01) == 1;
        thurst    = ((low  >> 7) & 0x01) == 1;
        direction = ((low  >> 6) & 0x01) == 1;
        relative  = ((low  >> 5) & 0x01) == 1;
        lock      = ((low  >> 4) & 0x01) == 1;
    }
}