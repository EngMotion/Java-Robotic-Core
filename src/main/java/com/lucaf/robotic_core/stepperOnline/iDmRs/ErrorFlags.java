package com.lucaf.robotic_core.stepperOnline.iDmRs;

import lombok.Getter;

/**
 * Wrapper around the current-alarm register (0x2203) of the iDM_RS drive.
 */
@Getter
public class ErrorFlags {

    /**
     * Raw value of the current-alarm register.
     */
    private final int alarmCode;

    /**
     * Constructor of the class.
     *
     * @param alarmCode raw value of the current-alarm register
     */
    public ErrorFlags(int alarmCode) {
        this.alarmCode = alarmCode;
    }

    /**
     * Returns whether the drive is currently reporting an alarm.
     *
     * @return true if the alarm register is non-zero
     */
    public boolean hasError() {
        return alarmCode != 0;
    }

    /**
     * Resolves the first fault code present in the alarm bitmask.
     *
     * @return the resolved {@link FaultCode}
     */
    public FaultCode getFaultCode() {
        return FaultCode.fromAlarm(alarmCode);
    }

    /**
     * Returns a human readable description of the current fault.
     *
     * @return the fault description, or "None" when no fault is present
     */
    public String getErrorDescription() {
        return getFaultCode().getDescription();
    }

    @Override
    public String toString() {
        return "ErrorFlags(alarmCode=" + alarmCode + ")";
    }
}
