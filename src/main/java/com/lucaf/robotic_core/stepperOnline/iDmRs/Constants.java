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
}
