package com.lucaf.robotic_core.stepperOnline.iDmRs;

public enum HomingMethod {
    NO_HOMING(-1),
    LIMIT_SWITCH(0),
    HOMING_SWITCH(1),
    SINGLE_TURN_Z_SIGNAL(2),
    TORQUE_DETECT(3),
    SET_CURRENT_POSITION(8);

    private final int value;

    HomingMethod(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static HomingMethod fromValue(int value) {
        for (HomingMethod method : values()) {
            if (method.value == value) {
                return method;
            }
        }
        throw new IllegalArgumentException("Valore HomingMethod non valido: " + value);
    }
}