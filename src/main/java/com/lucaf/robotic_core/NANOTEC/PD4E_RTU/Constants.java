package com.lucaf.robotic_core.NANOTEC.PD4E_RTU;

import com.lucaf.robotic_core.Pair;
import com.nanotec.nanolib.OdIndex;

public class Constants {
    public static final Pair<OdIndex, Integer> DEVICE_TYPE = new Pair<>(new OdIndex(0x1000, (short) 0x00),32);
    public static final Pair<OdIndex, Integer> ERROR_REGISTER = new Pair<>(new OdIndex(0x1001, (short) 0x00),8);
    public static final Pair<OdIndex, Integer> RESTORE_DEFAULTS = new Pair<>(new OdIndex(0x1011, (short) 0x00),32);
    /**
     * 0x2028:0x00 Value between 1 and 247
     */
    public static final Pair<OdIndex, Integer> SLAVE_ADDRESS = new Pair<>(new OdIndex(0x2028, (short) 0x00),8);
    public static final Pair<OdIndex, Integer> BAUDRATE = new Pair<>(new OdIndex(0x202A, (short) 0x00),32);
    public static final Pair<OdIndex, Integer> STOP_BITS = new Pair<>(new OdIndex(0x202C, (short) 0x00),8);
    public static final Pair<OdIndex, Integer> PARITY = new Pair<>(new OdIndex(0x202D, (short) 0x00),8);
    public static final Pair<OdIndex, Integer> POLE_PAIRS = new Pair<>(new OdIndex(0x2030, (short) 0x00),32);

    /**
     * Program the motor current in mA
     * Presets:
     * PD4-E591L42-E-65-5: 00000834h
     * PD4-E601L42-E-65-5: 00000834h
     * PD4-EB59CD-E-65-5: 00001770h
     * PD4-EB60SD-E-65-5: 00002D50h
     * PD4-E591L42-EB-65-5: 00000834h
     * PD4-EB59CD-EB-65-5: 00001770h
     */
    public static final Pair<OdIndex, Integer> MAX_MOTOR_CURRENT = new Pair<>(new OdIndex(0x2031, (short) 0x00),32);
    public static final Pair<OdIndex, Integer> UPPER_VOTLAGE_WARNING_LIMIT = new Pair<>(new OdIndex(0x2034, (short) 0x00),32);
    public static final Pair<OdIndex, Integer> LOWER_VOLTAGE_WARNING_LIMIT = new Pair<>(new OdIndex(0x2035, (short) 0x00),32);

    public static final Pair<OdIndex, Integer> HOMING_CURRENT_BLOCK_DETECTION_CURRENT = new Pair<>(new OdIndex(0x203A, (short) 0x01),32);
    public static final Pair<OdIndex, Integer> HOMING_CURRENT_BLOCK_DETECTION_TIME = new Pair<>(new OdIndex(0x203A, (short) 0x02),32);

    public static final Pair<OdIndex, Integer> CLOCK_DIRECTION = new Pair<>(new OdIndex(0x205A, (short) 0x00),32);
    public static final Pair<OdIndex, Integer> BOOTUP_DELAY = new Pair<>(new OdIndex(0x2084, (short) 0x00),32);

    public static final Pair<OdIndex, Integer> ERROR_CODE = new Pair<>(new OdIndex(0x603F, (short) 0x00),16);
    public static final Pair<OdIndex, Integer> CONTROL_WORD = new Pair<>(new OdIndex(0x6040, (short) 0x00),16);
    public static final Pair<OdIndex, Integer> STATUS_WORD = new Pair<>(new OdIndex(0x6041, (short) 0x00),16);

    public static final Pair<OdIndex, Integer> VELOCITY_MODE_SPEED = new Pair<>(new OdIndex(0x6042, (short) 0x00),16);
    public static final Pair<OdIndex, Integer> VELOCITY_MODE_ACCELERATION_DELTA_SPEED = new Pair<>(new OdIndex(0x6048, (short) 0x01),32);
    public static final Pair<OdIndex, Integer> VELOCITY_MODE_ACCELERATION_DELTA_TIME = new Pair<>(new OdIndex(0x6048, (short) 0x02),16);
    public static final Pair<OdIndex, Integer> VELOCITY_MODE_DECELERATION_DELTA_SPEED = new Pair<>(new OdIndex(0x6049, (short) 0x01),32);
    public static final Pair<OdIndex, Integer> VELOCITY_MODE_DECELERATION_DELTA_TIME = new Pair<>(new OdIndex(0x6049, (short) 0x02),16);


    /**
     * 0: Immediate stop with subsequent state change to Switch on disabled
     * 1: Braking with slow down ramp (deceleration ramp depending on operating mode) and subsequent state change to Switch on disabled
     * 2: Braking with quick stop ramp (6085h) and subsequent state change to Switch on disabled
     * 5: Braking with slow down ramp (deceleration ramp depending on operating mode) and subsequent state change to Quick stop active; control does not switch off and the motor remains energized. You can switch back to the Operation enabled state.
     * 6: Braking with quick stop ramp (6085h) and subsequent state change to Quick Stop Active; control does not switch off and the motor remains energized. You can switch back to the Operation enabled state.
     */
    public static final Pair<OdIndex, Integer> QUICK_STOP = new Pair<>(new OdIndex(0x605A, (short) 0x00),16);
    public static final Pair<OdIndex, Integer> SHUTDOWN = new Pair<>(new OdIndex(0x605B, (short) 0x00),16);
    public static final Pair<OdIndex, Integer> DISABLE = new Pair<>(new OdIndex(0x605C, (short) 0x00),16);

    public static class OperationMode {
        public static final int PROFILE_POSITION = 1;
        public static final int VELOCITY_MODE = 2;
        public static final int PROFILE_VELOCITY = 3;
        public static final int PROFILE_TORQUE = 4;
        public static final int HOMING = 6;
        public static final int INTERPOLATED_POSITION = 7;
        public static final int CYCLIC_SYNCHRONOUS_POSITION = 8;
        public static final int CYCLIC_SYNCHRONOUS_VELOCITY = 9;
        public static final int CYCLIC_SYNCHRONOUS_TORQUE = 10;
    }
    public static final Pair<OdIndex, Integer> MODE_OF_OPERATION = new Pair<>(new OdIndex(0x6060, (short) 0x00),8);
    public static final Pair<OdIndex, Integer> TARGET_POSITION = new Pair<>(new OdIndex(0x607A, (short) 0x00),32);
    public static final Pair<OdIndex, Integer> TARGET_TORQUE = new Pair<>(new OdIndex(0x6071, (short) 0x00),16);
    public static final Pair<OdIndex, Integer> TARGET_VELOCITY = new Pair<>(new OdIndex(0x60FF, (short) 0x00),32);
    public static final Pair<OdIndex, Integer> PROFILE_VELOCITY = new Pair<>(new OdIndex(0x6081, (short) 0x00),32);
    public static final Pair<OdIndex, Integer> PROFILE_ACCELERATION = new Pair<>(new OdIndex(0x6083, (short) 0x00),32);
    public static final Pair<OdIndex, Integer> PROFILE_DECELERATION = new Pair<>(new OdIndex(0x6084, (short) 0x00),32);
    public static final Pair<OdIndex, Integer> POSITION_ENCODER_RESOLUTION_INCREMENTS = new Pair<>(new OdIndex(0x608F, (short) 0x01),32);
    public static final Pair<OdIndex, Integer> POSITION_ENCODER_RESOLUTION_REVOLUTIONS = new Pair<>(new OdIndex(0x608F, (short) 0x02),32);
    public static final Pair<OdIndex, Integer> VELOCITY_ENCODER_RESOLUTION_INCREMENTS = new Pair<>(new OdIndex(0x6090, (short) 0x01),32);
    public static final Pair<OdIndex, Integer> VELOCITY_ENCODER_RESOLUTION_REVOLUTIONS = new Pair<>(new OdIndex(0x6090, (short) 0x02),32);
    public static final Pair<OdIndex, Integer> GEAR_RATIO_MOTION_REVOLUTIONS = new Pair<>(new OdIndex(0x6091, (short) 0x01),32);
    public static final Pair<OdIndex, Integer> GEAR_RATIO_SHAFT_REVOLUTIONS = new Pair<>(new OdIndex(0x6091, (short) 0x02),32);

    public static final Pair<OdIndex, Integer> DIGITAL_INPUTS = new Pair<>(new OdIndex(0x60FD, (short) 0x01),32);

    public static final Pair<OdIndex, Integer> INPUT_SPECIAL_FUNCTION = new Pair<>(new OdIndex(0x3240, (short) 0x01),32);



    public static final Pair<OdIndex, Integer> DIGITAL_OUTPUTS = new Pair<>(new OdIndex(0x60FE, (short) 0x01),32);
    /**
     * This subindex is used to invert the logic (from normally closed logic to normally open logic).
     */
    public static final Pair<OdIndex, Integer> DIGITAL_OUTPUT_INVERTED = new Pair<>(new OdIndex(0x3250, (short) 0x02),32);
    /**
     * This subindex is used to force the output value if the bit has the value "1".
     */
    public static final Pair<OdIndex, Integer> DIGITAL_OUTPUT_FORCE_ENABLE = new Pair<>(new OdIndex(0x3250, (short) 0x03),32);
    /**
     * The value "0" returns a logical low level at the digital output; the value "1", on the other hand, returns a logical high level.
     */
    public static final Pair<OdIndex, Integer> DIGITAL_OUTPUT_LEVEL = new Pair<>(new OdIndex(0x3250, (short) 0x04),32);
    /**
     * This subindex is used to read the raw value of the digital output.
     */
    public static final Pair<OdIndex, Integer> DIGITAL_OUTPUT_RAW = new Pair<>(new OdIndex(0x3250, (short) 0x06),32);
    /**
     * If the subindex is set to "1", Output Routing is activated.
     */
    public static final Pair<OdIndex, Integer> DIGITAL_OUTPUT_OUTING = new Pair<>(new OdIndex(0x3250, (short) 0x08),32);

    public static final Pair<OdIndex, Integer> HOME_OFFSET = new Pair<>(new OdIndex(0x607C, (short) 0x02),32);
    public static class HomingMethod {
        public static final int INDEX_SWITCH_CLOCKWISE = 19;
        public static final int INDEX_SWITCH_COUNTER_CLOCKWISE = 20;
    }
    public static final Pair<OdIndex, Integer> HOME_METHOD = new Pair<>(new OdIndex(0x6098, (short) 0x00),8);
}
