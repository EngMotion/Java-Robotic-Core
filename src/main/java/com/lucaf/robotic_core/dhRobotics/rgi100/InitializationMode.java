package com.lucaf.robotic_core.dhRobotics.rgi100;

public enum InitializationMode {
    CLAMPING_UNIDIRECTIONAL(0x01, "Clamping unidirectional return to zero (related to return to zero direction setting), rotating return to zero"),
    CLAMPING_OPEN_RETURN_TO_ZERO(0x02, "Clamping open return to zero (return to zero direction returns to open), rotating return to zero"),
    CLAMPING_CLOSED_RETURN_TO_ZERO(0x03, "Clamping closed return to zero (return to zero direction returns to closed), rotating return to zero"),
    CLAMPING_RECALIBRATION(0x04, "Clamping recalibration"),
    CLAMPING_OPEN(0x05, "Clamping open return to zero"),
    CLAMPING_CLOSED(0x06, "Clamping closed return to zero"),
    ROTATING_COUNTERCLOCKWISE(0x07, "Rotating counterclockwise return to zero"),
    RECALIBRATION_INITIALIZATION(0xA5, "Recalibration initialization (rotation first, then clamping)");

    private final int code;
    private final String description;

    InitializationMode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
