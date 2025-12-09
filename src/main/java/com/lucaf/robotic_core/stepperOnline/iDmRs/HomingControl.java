package com.lucaf.robotic_core.stepperOnline.iDmRs;

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
    HomingMethod homingMethod = HomingMethod.LIMIT_SWITCH;

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
        value |= homingMethod.getValue() << 2;
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
        homingMethod = HomingMethod.fromValue((value >> 2) & 0x3F);
        useZSignal = (value & 256) != 0;
    }
}
