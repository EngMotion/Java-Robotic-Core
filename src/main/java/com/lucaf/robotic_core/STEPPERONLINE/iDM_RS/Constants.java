package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

/**
 * Constants class containing the addresses of the registers of the iDM_RS controller.
 */
public class Constants {

    /**
     * Alarm: 0x2203
     * Value:
     * 0x01 Over- current
     * 0x02 Over- voltage
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

    public static final byte[] HOMING_METHOD = new byte[]{0x60, 0x0A};

    public static final byte[] HOMING_SPEED_HIGH = new byte[]{0x60, 0x0F};

    public static final byte[] HOMING_SPEED_LOW = new byte[]{0x60, 0x10};

    public static final byte[] HOMING_ACCELERATION = new byte[]{0x60, 0x11};

    public static final byte[] HOMING_DECELERATION = new byte[]{0x60, 0x12};

}
