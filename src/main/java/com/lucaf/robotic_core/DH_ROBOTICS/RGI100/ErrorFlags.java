package com.lucaf.robotic_core.DH_ROBOTICS.RGI100;

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
            case 4 -> "Overheat";
            case 8 -> "Overload";
            case 11 -> "Overspeed";
            default -> "Unknown error: " + errorCode;
        };
    }

}
