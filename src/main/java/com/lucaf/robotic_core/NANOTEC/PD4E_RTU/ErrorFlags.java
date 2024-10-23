package com.lucaf.robotic_core.NANOTEC.PD4E_RTU;

import lombok.Getter;

import java.util.Map;

@Getter
public class ErrorFlags {
    /**
     * Bit 0-15: Integer representing error code
     */
    private int errorCode;

    private static Map<Integer, String> errorMap = Map.ofEntries(
            Map.entry(0, "Watchdog-Reset"),
            Map.entry(1, "Input voltage (+Ub) too high"),
            Map.entry(2, "Output current too high"),
            Map.entry(3, "Input voltage (+Ub) too low"),
            Map.entry(4, "Error at fieldbus"),
            Map.entry(6, "CANopen only: NMT master takes too long to send Nodeguarding request"),
            Map.entry(7, "Sensor 1 Error through electrical fault or defective hardware"),
            Map.entry(8, "Sensor 2 Error through electrical fault or defective hardware"),
            Map.entry(9, "Sensor 3 Error through electrical fault or defective hardware"),
            Map.entry(10, "Warning: Positive limit switch exceeded"),
            Map.entry(11, "Warning: Negative limit switch exceeded"),
            Map.entry(12, "Overtemperature error"),
            Map.entry(13, "Timeout error"),
            Map.entry(14, "Warning: Nonvolatile memory full."),
            Map.entry(15, "Motor blocked"),
            Map.entry(16, "Warning: Nonvolatile memory damaged"),
            Map.entry(17, "CANopen only: Slave took too long to send PDO messages."),
            Map.entry(18, "Error through electrical fault or defective hardware"),
            Map.entry(19, "PDO not processed due to a length error"),
            Map.entry(20, "PDO length exceeded"),
            Map.entry(21, "Warning: error saving"),
            Map.entry(22, "Rated current must be set"),
            Map.entry(23, "Encoder resolution error"),
            Map.entry(24, "Motor current is too high"),
            Map.entry(25, "Internal software error, generic"),
            Map.entry(26, "Current too high at digital output"),
            Map.entry(27, "CANopen only: Unexpected sync length"),
            Map.entry(30, "Error in speed monitoring: slippage error too large"),
            Map.entry(32, "Internalerror: Correction factor for reference voltage missing in the OTP"),
            Map.entry(40, "Warning: Ballast resistor thermally overloaded"),
            Map.entry(46, "Interlock error"));

    public ErrorFlags(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean hasError() {
        return errorCode != 0;
    }

    public String getErrorDescription() {
        if (errorMap.containsKey(errorCode)) {
            return errorMap.get(errorCode);
        }
        return "Unknown error: " + errorCode;
    }
}
