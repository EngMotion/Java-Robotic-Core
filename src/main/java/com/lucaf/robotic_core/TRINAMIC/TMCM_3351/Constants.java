package com.lucaf.robotic_core.TRINAMIC.TMCM_3351;

/**
 * Class that contains the constants of the TMCM_3351 module
 */
public class Constants {

    /**
     * Target position: 0
     */
    public static final byte PARAM_TARGET_POSITION = (byte) 0;

    /**
     * Actual position: 1
     */
    public static final byte PARAM_ACTUAL_POSITION = (byte) 1;

    /**
     * Target speed: 2
     */
    public static final byte PARAM_TARGET_SPEED = (byte) 2;

    /**
     * Actual speed: 3
     */
    public static final byte PARAM_ACTUAL_SPEED = (byte) 3;

    /**
     * Maximum speed: 4
     */
    public static final byte PARAM_MAX_SPEED = (byte) 4;

    /**
     * Maximum acceleration: 5
     */
    public static final byte PARAM_MAX_ACCELERATION = (byte) 5;

    /**
     * Maximum current: 6
     */
    public static final byte PARAM_MAX_CURRENT = (byte) 6;

    /**
     * Standby current: 7
     */
    public static final byte PARAM_STANDBY_CURRENT = (byte) 7;

    /**
     * Position flag: 8
     */
    public static final byte PARAM_POSITION_FLAG = (byte) 8;

    /**
     * Home flag: 9
     */
    public static final byte PARAM_HOME_FLAG = (byte) 9;

    /**
     * Right limit state: 10
     */
    public static final byte PARAM_RIGHT_LIMIT_STATE = (byte) 10;

    /**
     * Left limit state: 11
     */
    public static final byte PARAM_LEFT_LIMIT_STATE = (byte) 11;

    /**
     * Right limit flag: 12
     */
    public static final byte PARAM_RIGHT_LIMIT_FLAG = (byte) 12;

    /**
     * Left limit flag: 13
     */
    public static final byte PARAM_LEFT_LIMIT_FLAG = (byte) 13;

    /**
     * Start velocity: 15
     */
    public static final byte PARAM_START_VELOCITY = (byte) 15;

    /**
     * Start acceleration: 16
     */
    public static final byte PARAM_START_ACCELERATION = (byte) 16;

    /**
     * Maximum deceleration: 17
     */
    public static final byte PARAM_MAX_DECELERATION = (byte) 17;

    /**
     * Break velocity: 18
     */
    public static final byte PARAM_BREAK_VELOCITY = (byte) 18;

    /**
     * Final deceleration: 19
     */
    public static final byte PARAM_FINAL_DECELERATION = (byte) 19;

    /**
     * Stop velocity: 20
     */
    public static final byte PARAM_STOP_VELOCITY = (byte) 20;

    /**
     * Stop deceleration: 21
     */
    public static final byte PARAM_STOP_DECELERATION = (byte) 21;

    /**
     * Closed Loop Minimum Current: 113
     */
    public static final byte PARAM_CL_MIN_CURRENT = (byte) 113;

    /**
     * Closed Loop Maximum Current: 114
     */
    public static final byte PARAM_CL_MAX_CURRENT = (byte) 114;

    /**
     * Closed Loop Correction Position: 124
     */
    public static final byte PARAM_CL_CORRECTION_POSITION = (byte) 124;

    /**
     * Closed Loop Correction Tolerance: 125
     */
    public static final byte PARAM_CL_CORRECTION_TOLERANCE = (byte) 125;

    /**
     * Closed Loop Start Up: 126
     */
    public static final byte PARAM_CL_START_UP = (byte) 126;

    /**
     * Relative Positioning Option: 127
     */
    public static final byte PARAM_RELATIVE_POSITIONING_OPTION = (byte) 127;

    /**
     * Closed Loop Mode: 129
     */
    public static final byte PARAM_CL_MODE = (byte) 129;

    /**
     * Encoder Speed: 131
     */
    public static final byte PARAM_ENCODER_SPEED = (byte) 131;

    /**
     * Closed Loop Initialization: 133
     */
    public static final byte PARAM_CLOSED_LOOP_INIT = (byte) 133;

    /**
     * Microstep Resolution: 140
     */
    public static final byte PARAM_MICROSTEP_RESOLUTION = (byte) 140;

    /**
     * Smart Energy Current: 168
     */
    public static final byte PARAM_SMART_ENERGY_CURRENT = (byte) 168;

    /**
     * Smart Energy Stall Level: 169
     */
    public static final byte PARAM_SMART_ENERGY_STALL_LEVEL = (byte) 169;

    /**
     * Smart Energy Hysteresis: 170
     */
    public static final byte PARAM_SMART_ENERGY_HYSTERESIS = (byte) 170;

    /**
     * Smart Energy Step Up: 171
     */
    public static final byte PARAM_SMART_ENERGY_STEP_UP = (byte) 171;

    /**
     * Smart Energy Hysteresis Start: 172
     */
    public static final byte PARAM_SMART_ENERGY_HYSTERESIS_START = (byte) 172;

    /**
     * StallGuard Filter Enable: 173
     */
    public static final byte PARAM_STALLGUARD_FILTER_ENABLE = (byte) 173;

    /**
     * StallGuard Threshold: 174
     */
    public static final byte PARAM_STALLGUARD_THRESHOLD = (byte) 174;

    /**
     * Smart Energy Actual Current: 180
     */
    public static final byte PARAM_SMART_ENERGY_ACTUAL_CURRENT = (byte) 180;

    /**
     * Stop on Stall: 181
     */
    public static final byte PARAM_STOP_ON_STALL = (byte) 181;

    /**
     * Reference Search Mode: 193
     */
    public static final byte PARAM_REFERENCE_SEARCH_MODE = (byte) 193;

    /**
     * Reference Search Speed: 194
     */
    public static final byte PARAM_REFERENCE_SEARCH_SPEED = (byte) 194;

    /**
     * Reference Switch Speed: 195
     */
    public static final byte PARAM_REFERENCE_SWITCH_SPEED = (byte) 195;

    /**
     * End Switch Distance: 196
     */
    public static final byte PARAM_END_SWITCH_DISTANCE = (byte) 196;

    /**
     * Last Reference Position: 197
     */
    public static final byte PARAM_LAST_REFERENCE_POSITION = (byte) 197;

    /**
     * Boost Current: 200
     */
    public static final byte PARAM_BOOST_CURRENT = (byte) 200;

    /**
     * Encoder Mode: 201
     */
    public static final byte PARAM_ENCODER_MODE = (byte) 201;

    /**
     * Full Step Resolution: 202
     */
    public static final byte PARAM_FULL_STEP_RESOLUTION = (byte) 202;

    /**
     * Freewheeling: 204
     */
    public static final byte PARAM_FREEWHEELING = (byte) 204;

    /**
     * Load Value: 206
     */
    public static final byte PARAM_LOAD_VALUE = (byte) 206;

    /**
     * Extended Error Flag: 207
     */
    public static final byte PARAM_EXTENDED_ERROR_FLAG = (byte) 207;

    /**
     * Error Flags: 208
     */
    public static final byte PARAM_ERROR_FLAGS = (byte) 208;

    /**
     * Encoder Position: 209
     */
    public static final byte PARAM_ENCODER_POSITION = (byte) 209;

    /**
     * Encoder Resolution: 210
     */
    public static final byte PARAM_ENCODER_RESOLUTION = (byte) 210;

    /**
     * Encoder Direction: 211
     */
    public static final byte PARAM_ENCODER_MAX_DEVIATION = (byte) 212;

    /**
     * Velocity Max Deviation: 213
     */
    public static final byte PARAM_VELOCITY_MAX_DEVIATION = (byte) 213;

    /**
     * Reverse Shaft: 251
     */
    public static final byte PARAM_REVERSE_SHAFT = (byte) 251;

    /**
     * Serial address of the device: 66
     */
    public static final byte GLOBAL_SERIAL_ADDRESS = (byte) 66;

    /**
     * Movement absolute: 1
     */
    public static final byte MVP_ABS = 1;

    /**
     * Movement relative: 2
     */
    public static final byte MVP_REL = 2;

    /**
     * Movement coordinate: 3
     */
    public static final byte MVP_COORD = 3;

    /**
     * Rotate right: 1
     */
    public static final byte ROR= 1;

    /**
     * Rotate left: 2
     */
    public static final byte ROL= 2;

    /**
     * Stop Motor: 3
     */
    public static final byte MST= 3;

    /**
     * Move to position: 4
     */
    public static final byte MVP= 4;

    /**
     * Set axis parameter: 5
     */
    public static final byte SAP= 5;

    /**
     * Get axis parameter: 6
     */
    public static final byte GAP= 6;

    /**
     * Set global parameter: 7
     */
    public static final byte SGP= 7;

    /**
     * Get global parameter: 8
     */
    public static final byte GGP= 8;

    /**
     * Store global parameter: 9
     */
    public static final byte STGP= 9;

    /**
     * Restore global parameter: 10
     */
    public static final byte RSGP= 10;

    /**
     * Reference search: 11
     */
    public static final byte RFS= 11;

    /**
     * Set digital output: 12
     */
    public static final byte SIO= 12;

    /**
     * Get digital input: 13
     */
    public static final byte GIO= 13;
}
