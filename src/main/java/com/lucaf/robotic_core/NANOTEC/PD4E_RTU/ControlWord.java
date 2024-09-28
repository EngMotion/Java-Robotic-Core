package com.lucaf.robotic_core.NANOTEC.PD4E_RTU;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ControlWord {
    /*
     * Bit 0: Switch on
     * Value 0: The drive is switched off.
     * Value 1: The drive is switched on.
     */
    boolean switchOn = false;

    /*
     * Bit 1: Enable voltage
     * Value 0: The drive is voltage enabled.
     * Value 1: The drive is voltage disabled.
     */
    boolean enableVoltage = false;

    /*
     * Bit 2: Quick stop
     * Value 0: The drive is not in quick stop state.
     * Value 1: The drive is in quick stop state.
     */
    boolean quickStop = false;

    /*
     * Bit 3: Enable operation
     * Value 0: The drive is disabled.
     * Value 1: The drive is enabled.
     */
    boolean enableOperation = false;

    /*
     * Bit 4: Operation mode specific
     */
    boolean operationModeSpecific_4 = false;

    /*
     * Bit 5: Operation mode specific
     */
    boolean operationModeSpecific_5 = false;

    /*
     * Bit 6: Operation mode specific
     */
    boolean operationModeSpecific_6 = false;

    /*
     * Bit 7: Fault reset
     * Value 0: No fault reset.
     * Value 1: Reset the fault.
     */
    boolean faultReset = false;

    /*
     * Bit 8: Halt
     * Value 0: The drive is not halted.
     * Value 1: The drive is halted.
     */
    boolean halt = false;

    public int toInt() {
        int controlWord = 0;
        controlWord |= switchOn ? 1 : 0;
        controlWord |= enableVoltage ? 1 << 1 : 0;
        controlWord |= quickStop ? 1 << 2 : 0;
        controlWord |= enableOperation ? 1 << 3 : 0;
        controlWord |= operationModeSpecific_4 ? 1 << 4 : 0;
        controlWord |= operationModeSpecific_5 ? 1 << 5 : 0;
        controlWord |= operationModeSpecific_6 ? 1 << 6 : 0;
        controlWord |= faultReset ? 1 << 7 : 0;
        controlWord |= halt ? 1 << 8 : 0;
        return controlWord;
    }

    @Override
    public String toString(){
        return "ControlWord{" +
                "switchOn=" + switchOn +
                ", enableVoltage=" + enableVoltage +
                ", quickStop=" + quickStop +
                ", enableOperation=" + enableOperation +
                ", operationModeSpecific_4=" + operationModeSpecific_4 +
                ", operationModeSpecific_5=" + operationModeSpecific_5 +
                ", operationModeSpecific_6=" + operationModeSpecific_6 +
                ", faultReset=" + faultReset +
                ", halt=" + halt +
                "}";
    }

    public ControlWord(int controlWord) {
        switchOn = (controlWord & 1) == 1;
        enableVoltage = (controlWord & 1 << 1) == 1 << 1;
        quickStop = (controlWord & 1 << 2) == 1 << 2;
        enableOperation = (controlWord & 1 << 3) == 1 << 3;
        operationModeSpecific_4 = (controlWord & 1 << 4) == 1 << 4;
        operationModeSpecific_5 = (controlWord & 1 << 5) == 1 << 5;
        operationModeSpecific_6 = (controlWord & 1 << 6) == 1 << 6;
        faultReset = (controlWord & 1 << 7) == 1 << 7;
        halt = (controlWord & 1 << 8) == 1 << 8;
    }

    public ControlWord(boolean switchOn, boolean enableVoltage, boolean quickStop, boolean enableOperation, boolean operationModeSpecific_4, boolean operationModeSpecific_5, boolean operationModeSpecific_6, boolean faultReset, boolean halt) {
        this.switchOn = switchOn;
        this.enableVoltage = enableVoltage;
        this.quickStop = quickStop;
        this.enableOperation = enableOperation;
        this.operationModeSpecific_4 = operationModeSpecific_4;
        this.operationModeSpecific_5 = operationModeSpecific_5;
        this.operationModeSpecific_6 = operationModeSpecific_6;
        this.faultReset = faultReset;
        this.halt = halt;
    }

    public ControlWord() {
    }

}
