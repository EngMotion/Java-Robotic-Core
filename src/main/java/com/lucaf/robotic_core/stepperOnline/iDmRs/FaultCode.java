package com.lucaf.robotic_core.stepperOnline.iDmRs;

import lombok.Getter;

/**
 * Enumeration of the fault/alarm codes reported by the iDM_RS drive through the
 * current-alarm register (0x2203).
 */
@Getter
public enum FaultCode {

    /**
     * No fault present.
     */
    NONE(0, "None"),

    /**
     * Over-current.
     */
    OVER_CURRENT(0x01, "Over-current"),

    /**
     * Over-voltage.
     */
    OVER_VOLTAGE(0x02, "Over-voltage"),

    /**
     * Current sampling fault.
     */
    CURRENT_SAMPLING_FAULT(0x40, "Current sampling fault"),

    /**
     * Failed to lock shaft.
     */
    FAILED_TO_LOCK_SHAFT(0x80, "Failed to lock shaft"),

    /**
     * Auto-tuning fault.
     */
    AUTO_TUNING_FAULT(0x100, "Auto-tuning fault"),

    /**
     * EEPROM fault.
     */
    EEPROM_FAULT(0x200, "EEPROM fault"),

    /**
     * Unknown/unmapped fault code.
     */
    UNKNOWN(-1, "Unknown");

    /**
     * The raw alarm bit value for this fault code (or {@code 0} / {@code -1} for sentinels).
     */
    private final int code;

    /**
     * Human readable description of the fault.
     */
    private final String description;

    FaultCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Resolves the first fault code contained in the current-alarm bitmask.
     *
     * @param alarm raw value of the current-alarm register (0x2203)
     * @return the first matching {@link FaultCode}, {@link #NONE} if no bit is set,
     * or {@link #UNKNOWN} if the bitmask contains no known code
     */
    public static FaultCode fromAlarm(int alarm) {
        if (alarm == 0) {
            return NONE;
        }
        for (FaultCode faultCode : values()) {
            if (faultCode.code > 0 && (alarm & faultCode.code) != 0) {
                return faultCode;
            }
        }
        return UNKNOWN;
    }
}
