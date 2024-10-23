package com.lucaf.robotic_core.DH_ROBOTICS.SAC_N_M2;

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

}
