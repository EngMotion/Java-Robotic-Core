package com.lucaf.robotic_core.NANOTEC.PD4E_RTU;

public class StatusWord {
    /**
     * Bit 0: Ready to switch on
     * Value 0: The drive is not ready to switch on.
     * Value 1: The drive is ready to switch on.
     */
    boolean readyToSwitchOn = false;

    /**
     * Bit 1: Switched on
     * Value 0: The drive is switched off.
     * Value 1: The drive is switched on.
     */
    boolean switchedOn = false;

    /**
     * Bit 2: Operation enabled
     * Value 0: The drive is disabled.
     * Value 1: The drive is enabled.
     */
    boolean operationEnabled = false;

    /**
     * Bit 3: Fault
     * Value 0: No fault.
     * Value 1: Fault.
     */
    boolean fault = false;

    /**
     * Bit 4: Voltage enabled
     * Value 0: The drive is voltage disabled.
     * Value 1: The drive is voltage enabled.
     */
    boolean voltageEnabled = false;

    /**
     * Bit 5: Quick stop
     * Value 0: The drive is not in quick stop state.
     * Value 1: The drive is in quick stop state.
     */
    boolean quickStop = false;

    /**
     * Bit 6: Switch on disabled
     * Value 0: The drive is not in switch on disabled state.
     * Value 1: The drive is in switch on disabled state.
     */
    boolean switchOnDisabled = false;

    /**
     * Bit 7: Warning
     * Value 0: No warning.
     * Value 1: Warning.
     */
    boolean warning = false;

    /**
     * Bit 8: Sync
     * Value 0: Controller not in sync with fieldbus.
     * Value 1: Controller in sync with fieldbus.
     */
    boolean sync = false;

    /**
     * Bit 10: Target reached
     */
    boolean targetReached = false;

    /**
     * Bit 11: Internal limit active
     */
    boolean internalLimitActive = false;

    /**
     * Bit 13: Closed loop enabled
     */
    boolean closedLoopEnabled = false;

    public static class States {
        public static final int NOT_READY_TO_SWITCH_ON = 0;
        public static final int SWITCH_ON_DISABLED = 1;
        public static final int READY_TO_SWITCH_ON = 2;
        public static final int SWITCHED_ON = 3;
        public static final int OPERATION_ENABLED = 4;
        public static final int QUICK_STOP_ACTIVE = 5;
        public static final int FAULT_REACTION_ACTIVE = 6;
        public static final int FAULT = 7;
        public static final int OTHER = -1;
    }

    public int toInt(){
        int statusWord = 0;
        statusWord |= readyToSwitchOn ? 1 : 0;
        statusWord |= switchedOn ? 1 << 1 : 0;
        statusWord |= operationEnabled ? 1 << 2 : 0;
        statusWord |= fault ? 1 << 3 : 0;
        statusWord |= voltageEnabled ? 1 << 4 : 0;
        statusWord |= quickStop ? 1 << 5 : 0;
        statusWord |= switchOnDisabled ? 1 << 6 : 0;
        statusWord |= warning ? 1 << 7 : 0;
        statusWord |= sync ? 1 << 8 : 0;
        statusWord |= targetReached ? 1 << 10 : 0;
        statusWord |= internalLimitActive ? 1 << 11 : 0;
        statusWord |= closedLoopEnabled ? 1 << 13 : 0;
        return statusWord;
    }

    public int getStateCode(){
        if (!readyToSwitchOn && !switchedOn && !operationEnabled && !fault && !switchOnDisabled) return States.NOT_READY_TO_SWITCH_ON;
        if (!readyToSwitchOn && !switchedOn && !operationEnabled && !fault && switchOnDisabled) return States.SWITCH_ON_DISABLED;
        if (readyToSwitchOn && !switchedOn && !operationEnabled && !fault && quickStop && !switchOnDisabled) return States.READY_TO_SWITCH_ON;
        if (readyToSwitchOn && switchedOn && !operationEnabled && !fault && quickStop && !switchOnDisabled) return States.SWITCHED_ON;
        if (readyToSwitchOn && switchedOn && operationEnabled && !fault && !quickStop && !switchOnDisabled) return States.OPERATION_ENABLED;
        if (readyToSwitchOn && switchedOn && operationEnabled && !fault && quickStop && !switchOnDisabled) return States.QUICK_STOP_ACTIVE;
        if (readyToSwitchOn && switchedOn && operationEnabled && fault && !switchOnDisabled) return States.FAULT_REACTION_ACTIVE;
        if (!readyToSwitchOn && !switchedOn && !operationEnabled && fault && !switchOnDisabled) return States.FAULT;
        return States.OTHER;
    }

    @Override
    public String toString(){
        return "StatusWord{" +
                "readyToSwitchOn=" + readyToSwitchOn +
                ", switchedOn=" + switchedOn +
                ", operationEnabled=" + operationEnabled +
                ", fault=" + fault +
                ", voltageEnabled=" + voltageEnabled +
                ", quickStop=" + quickStop +
                ", switchOnDisabled=" + switchOnDisabled +
                ", warning=" + warning +
                ", sync=" + sync +
                ", targetReached=" + targetReached +
                ", internalLimitActive=" + internalLimitActive +
                ", closedLoopEnabled=" + closedLoopEnabled +
                "}";
    }

    public StatusWord(int statusWord) {
        readyToSwitchOn = (statusWord & 1) == 1;
        switchedOn = (statusWord & 1 << 1) == 1 << 1;
        operationEnabled = (statusWord & 1 << 2) == 1 << 2;
        fault = (statusWord & 1 << 3) == 1 << 3;
        voltageEnabled = (statusWord & 1 << 4) == 1 << 4;
        quickStop = (statusWord & 1 << 5) == 1 << 5;
        switchOnDisabled = (statusWord & 1 << 6) == 1 << 6;
        warning = (statusWord & 1 << 7) == 1 << 7;
        sync = (statusWord & 1 << 8) == 1 << 8;
        targetReached = (statusWord & 1 << 10) == 1 << 10;
        internalLimitActive = (statusWord & 1 << 11) == 1 << 11;
        closedLoopEnabled = (statusWord & 1 << 13) == 1 << 13;
    }

    public StatusWord(boolean readyToSwitchOn, boolean switchedOn, boolean operationEnabled, boolean fault, boolean voltageEnabled, boolean quickStop, boolean switchOnDisabled, boolean warning, boolean sync, boolean targetReached, boolean internalLimitActive, boolean closedLoopEnabled) {
        this.readyToSwitchOn = readyToSwitchOn;
        this.switchedOn = switchedOn;
        this.operationEnabled = operationEnabled;
        this.fault = fault;
        this.voltageEnabled = voltageEnabled;
        this.quickStop = quickStop;
        this.switchOnDisabled = switchOnDisabled;
        this.warning = warning;
        this.sync = sync;
        this.targetReached = targetReached;
        this.internalLimitActive = internalLimitActive;
        this.closedLoopEnabled = closedLoopEnabled;
    }

    public StatusWord() {
    }

}
