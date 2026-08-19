package com.lucaf.robotic_core.stepperOnline.iDmRs;

/**
 * Constants class containing the addresses of the registers of the IDM_RS controller.
 */
public class Constants {
    /**
     * Settings for Digital Input 1. Controlled via DigitalInput class
     */
    public static final byte[] DI1 = new byte[]{0x01, 0x45};

    /**
     * Settings for Digital Input 2. Controlled via DigitalInput class
     */
    public static final byte[] DI2 = new byte[]{0x01, 0x47};

    /**
     * Settings for Digital Input 3. Controlled via DigitalInput class
     */
    public static final byte[] DI3 = new byte[]{0x01, 0x49};

    /**
     * Settings for Digital Input 4. Controlled via DigitalInput class
     */
    public static final byte[] DI4 = new byte[]{0x01, 0x4B};

    /**
     * Settings for Digital Input 5. Controlled via DigitalInput class
     */
    public static final byte[] DI5 = new byte[]{0x01, 0x4D};

    /**
     * Settings for Digital Input 6. Controlled via DigitalInput class
     */
    public static final byte[] DI6 = new byte[]{0x01, 0x4F};

    /**
     * Settings for Digital Input 7. Controlled via DigitalInput class
     */
    public static final byte[] DI7 = new byte[]{0x01, 0x51};

    /**
     * Settings for Digital Output 1. Controlled via DigitalOutput class
     */
    public static final byte[] DO1 = new byte[]{0x01, 0x57};

    /**
     * Settings for Digital Output 2. Controlled via DigitalOutput class
     */
    public static final byte[] DO2 = new byte[]{0x01, 0x59};

    /**
     * Settings for Digital Output 3. Controlled via DigitalOutput class
     */
    public static final byte[] DO3 = new byte[]{0x01, 0x5B};

    public static final byte[] ACTUAL_POSITION_HIGH = new byte[]{0x60, (byte) 0x2C};
    public static final byte[] ACTUAL_POSITION_LOW = new byte[]{0x60, (byte) 0x2D};


    /**
     * Digital Inputs Status, reads current state of the digital inputs. Controlled via DigitalInputs class
     */
    public static final byte[] DIGITAL_INPUTS_STATUS = new byte[]{0x01, 0x79};

    /**
     * Digital Outputs Status, reads current state of the digital outputs. Controlled via DigitalOutputs class
     */
    public static final byte[] DIGITAL_OUTPUTS_STATUS = new byte[]{0x01, 0x7B};

    /**
     * Alarm: 0x2203
     * Value:
     * 0x01 Over-current
     * 0x02 Over-voltage
     * 0x40 Current sampling fault
     * 0x80 Failed to lock shaft
     * 0x200 EEPROM fault
     * 0x100 Auto-tuning fault
     * Type: Read
     */
    public static final byte[] ALARM = new byte[]{0x22, 0x03};

    /**
     * Alarm detection selection: 0x016D (Pr4.22)
     * Bitmask enabling detection of each alarm:
     * bit0 over-current, bit1 over-voltage, bit3 ADC sampling failure,
     * bit4 locked shaft, bit5 EEPROM, bit6 auto-tuning.
     * Type: Read/Write
     */
    public static final byte[] ALARM_DETECTION_SELECTION = new byte[]{0x01, 0x6D};

    /**
     * Motion status: 0x1003
     * Bit0 = 1 indicates the drive is in "faulty" state (quick fault flag).
     * Type: Read
     */
    public static final byte[] MOTION_STATUS = new byte[]{0x10, 0x03};

    /**
     * PR warning: 0x601D (Pr8.29)
     * PR-module specific warnings:
     * 0x100 limit switch error during homing, 0x102 over-travel error during homing,
     * 0x20P limit switch error in path P.
     * Type: Read
     */
    public static final byte[] PR_WARNING = new byte[]{0x60, 0x1D};

    /**
     * Control word: 0x1801
     * Write-only command register (see CONTROL_WORD_* constants).
     */
    public static final byte[] CONTROL_WORD = new byte[]{0x18, 0x01};

    /**
     * Control word command: reset the current alarm.
     */
    public static final int CONTROL_WORD_RESET_ALARM = 0x1111;

    /**
     * Control word command: reset the alarm history.
     */
    public static final int CONTROL_WORD_RESET_ALARM_HISTORY = 0x1122;

    /**
     * Control word command: save parameters to EEPROM.
     */
    public static final int CONTROL_WORD_SAVE_EEPROM = 0x2211;

    /**
     * Control Mode: 0x6200
     * Value: Managed by the ControlMode class
     * Type: Read/Write
     */
    public static final byte[] CONTROL_MODE = new byte[]{0x62, 0x00};

    /**
     * Status Mode: 0x6002
     * Value: Managed by the StatusMode class
     */
    public static final byte[] STATUS_MODE = new byte[]{0x60, 0x02};

    /**
     * Target Position High: 0x6201
     * Value: High 16 bits of the target position
     * Type: Read/Write
     */
    public static final byte[] TARGET_POSITION_HIGH = new byte[]{0x62, 0x01};

    /**
     * Target Position Low: 0x6202
     * Value: High 16 bits of the target position
     * Type: Read/Write
     */
    public static final byte[] TARGET_POSITION_LOW = new byte[]{0x62, 0x02};

    /**
     * Velocity: 0x6203
     * Value: rpm
     * Type: Read/Write
     */
    public static final byte[] VELOCITY = new byte[]{0x62, 0x03};

    /**
     * Acceleration: 0x6204
     * Value: ms/1000rpm
     * Type: Read/Write
     */
    public static final byte[] ACCELERATION = new byte[]{0x62, 0x04};

    /**
     * Deceleration: 0x6205
     * Value: ms/1000rpm
     * Type: Read/Write
     */
    public static final byte[] DECELERATION = new byte[]{0x62, 0x05};

    /**
     * Pause Time: 0x6206
     * Value: ms
     * Type: Read/Write
     */
    public static final byte[] PAUSE_TIME = new byte[]{0x62, 0x06};

    /**
     * Homing Method
     */
    public static final byte[] HOMING_METHOD = new byte[]{0x60, 0x0A};

    /**
     * Position to reach after homing. High 16 bits
     */
    public static final byte[] HOMING_STOP_POSITION_HIGH = new byte[]{0x60, 0x0D};

    /**
     * Position to reach after homing. Low 16 bits
     */
    public static final byte[] HOMING_STOP_POSITION_LOW = new byte[]{0x60, 0x0E};

    /**
     * Homing speed value high
     */
    public static final byte[] HOMING_SPEED_HIGH = new byte[]{0x60, 0x0F};

    /**
     * Homing speed value low
     */
    public static final byte[] HOMING_SPEED_LOW = new byte[]{0x60, 0x10};

    /**
     * Homing acceleration value
     */
    public static final byte[] HOMING_ACCELERATION = new byte[]{0x60, 0x11};

    /**
     * Homing deceleration value
     */
    public static final byte[] HOMING_DECELERATION = new byte[]{0x60, 0x12};


    public static final byte[] PEAK_CURRENT = new byte[]{0x01, (byte) 0x91};
}
