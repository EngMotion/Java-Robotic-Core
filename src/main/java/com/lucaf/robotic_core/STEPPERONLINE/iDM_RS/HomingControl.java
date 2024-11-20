package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomingControl {
    /**
     * BIT0: 0: Negative direction, 1: Positive direction
     */
    boolean positiveDirection = false;

    /**
     * BIT1: Go to set position after homing
     */
    boolean goToSetPosition = false;

    /**
     * BIT2-7: Homing method
     * 0： homing with limit switch detect
     * 1： homing with homing switch detect
     * 2： homing with single turn Z signal detect
     * 3： homing with torque detect
     * 8： set current position as homing position
     */
    int homingMethod = 0;

    public static class HomingMethod {
        public static final int LIMIT_SWITCH = 0;
        public static final int HOMING_SWITCH = 1;
        public static final int SINGLE_TURN_Z_SIGNAL = 2;
        public static final int TORQUE_DETECT = 3;
        public static final int SET_CURRENT_POSITION = 8;
    }

    /**
     * BIT8: Use Z signal
     */
    boolean useZSignal = false;

    /**
     * Convert the object to an integer
     * @return The integer value
     */
    public int toInt() {
        int value = 0;
        if (positiveDirection) {
            value |= 1;
        }
        if (goToSetPosition) {
            value |= 2;
        }

        value |= homingMethod << 2;

        if (useZSignal) {
            value |= 256;
        }

        return value;
    }

    /**
     * Create a new HomingControl object from an integer
     * @param value The integer value
     */
    public HomingControl(int value) {
        positiveDirection = (value & 1) != 0;
        goToSetPosition = (value & 2) != 0;
        homingMethod = (value >> 2) & 0x3F;
        useZSignal = (value & 256) != 0;
    }
}
