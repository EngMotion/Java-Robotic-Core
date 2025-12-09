package com.lucaf.robotic_core.stepperOnline.iDmRs;

public enum ControlType {
    NO_MODE(0),
    VELOCITY_MODE(2),
    POSITION_MODE(1);

    private final int value;

    ControlType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}