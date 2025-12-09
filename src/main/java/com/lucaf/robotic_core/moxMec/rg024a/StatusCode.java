package com.lucaf.robotic_core.moxMec.rg024a;

public enum StatusCode {
    OFF(0),
    ON(1),
    ERROR(2),
    WRONG_DATA(3),
    EEPROM_ERROR(4),
    OUTPUT_ERROR(5);

    private final int code;

    StatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static StatusCode fromValue(int code) {
        for (StatusCode status : StatusCode.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null; // or throw an exception
    }
}