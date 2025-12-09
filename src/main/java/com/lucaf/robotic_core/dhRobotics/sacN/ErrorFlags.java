package com.lucaf.robotic_core.dhRobotics.sacN;

import lombok.Getter;

@Getter
public class ErrorFlags {
    private int errorCode;

    public ErrorFlags(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean hasError() {
        return errorCode != 0;
    }

    public String getErrorDescription() {
        return switch (errorCode) {
            default -> "Unknown error: " + errorCode;
        };
    }

    @Override
    public String toString() {
        return "ErrorFlags(errorCode=" + this.errorCode + ")";
    }

}
