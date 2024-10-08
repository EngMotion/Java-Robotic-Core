package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

/**
 * Constants class containing the addresses of the registers of the iDM_RS controller.
 */
public class Constants {

    /**
     * Motor direction: 0x0007
     * Value:
     * 0x00: Clockwise
     * 0x01: Counter-clockwise
     * Type: Read/Write
     */
    public static final byte[] MOTOR_DIRECTION = new byte[]{0x00, 0x07};

    /**
     * Digital Input 1: 0x0145
     * Value:
     * 0: invalid;
     * 7: alarm clearing;
     * 8: enable (also can be set by 0x00F);
     * 0x20: Trigger command (CTRG);
     * 0x21: Trigger homing;
     * 0x22: EMG (quick stop);
     * 0x23: JOG+;
     * 0x24: JOG-;
     * 0x25: POT (positive limit);
     * 0x26: NOT (negative limit);
     * 0x27: ORG (home switch);
     * 0x28: ADD0 (path address 0);
     * 0x29: ADD1 (path address 1);
     * 0x2A: ADD2 (path address 2);
     * 0x2B: ADD3 (path address 3);
     * 0x2C: JOG velocity 2
     * Type: Read/Write
     */
    public static final byte[] DI_1 = new byte[]{0x01, 0x45};

    /**
     * Digital Input 2: 0x0146
     * Value:
     * 0: invalid;
     * 7: alarm clearing;
     * 8: enable (also can be set by 0x00F);
     * 0x20: Trigger command (CTRG);
     * 0x21: Trigger homing;
     * 0x22: EMG (quick stop);
     * 0x23: JOG+;
     * 0x24: JOG-;
     * 0x25: POT (positive limit);
     * 0x26: NOT (negative limit);
     * 0x27: ORG (home switch);
     * 0x28: ADD0 (path address 0);
     * 0x29: ADD1 (path address 1);
     * 0x2A: ADD2 (path address 2);
     * 0x2B: ADD3 (path address 3);
     * 0x2C: JOG velocity 2
     * Type: Read/Write
     */
    public static final byte[] DI_2 = new byte[]{0x01, 0x47};

    /**
     * Digital Input 3: 0x0149
     * Value:
     * 0: invalid;
     * 7: alarm clearing;
     * 8: enable (also can be set by 0x00F);
     * 0x20: Trigger command (CTRG);
     * 0x21: Trigger homing;
     * 0x22: EMG (quick stop);
     * 0x23: JOG+;
     * 0x24: JOG-;
     * 0x25: POT (positive limit);
     * 0x26: NOT (negative limit);
     * 0x27: ORG (home switch);
     * 0x28: ADD0 (path address 0);
     * 0x29: ADD1 (path address 1);
     * 0x2A: ADD2 (path address 2);
     * 0x2B: ADD3 (path address 3);
     * 0x2C: JOG velocity 2
     * Type: Read/Write
     */
    public static final byte[] DI_3 = new byte[]{0x01, 0x49};

    /**
     * Digital Input 4: 0x014B
     * Value:
     * 0: invalid;
     * 7: alarm clearing;
     * 8: enable (also can be set by 0x00F);
     * 0x20: Trigger command (CTRG);
     * 0x21: Trigger homing;
     * 0x22: EMG (quick stop);
     * 0x23: JOG+;
     * 0x24: JOG-;
     * 0x25: POT (positive limit);
     * 0x26: NOT (negative limit);
     * 0x27: ORG (home switch);
     * 0x28: ADD0 (path address 0);
     * 0x29: ADD1 (path address 1);
     * 0x2A: ADD2 (path address 2);
     * 0x2B: ADD3 (path address 3);
     * 0x2C: JOG velocity 2
     * Type: Read/Write
     */
    public static final byte[] DI_4 = new byte[]{0x01, 0x4B};

    /**
     * Digital Input 5: 0x014D
     * Value:
     * 0: invalid;
     * 7: alarm clearing;
     * 8: enable (also can be set by 0x00F);
     * 0x20: Trigger command (CTRG);
     * 0x21: Trigger homing;
     * 0x22: EMG (quick stop);
     * 0x23: JOG+;
     * 0x24: JOG-;
     * 0x25: POT (positive limit);
     * 0x26: NOT (negative limit);
     * 0x27: ORG (home switch);
     * 0x28: ADD0 (path address 0);
     * 0x29: ADD1 (path address 1);
     * 0x2A: ADD2 (path address 2);
     * 0x2B: ADD3 (path address 3);
     * 0x2C: JOG velocity 2
     * Type: Read/Write
     */
    public static final byte[] DI_5 = new byte[]{0x01, 0x4D};

    /**
     * Digital Input 6: 0x014F
     * Value:
     * 0: invalid;
     * 7: alarm clearing;
     * 8: enable (also can be set by 0x00F);
     * 0x20: Trigger command (CTRG);
     * 0x21: Trigger homing;
     * 0x22: EMG (quick stop);
     * 0x23: JOG+;
     * 0x24: JOG-;
     * 0x25: POT (positive limit);
     * 0x26: NOT (negative limit);
     * 0x27: ORG (home switch);
     * 0x28: ADD0 (path address 0);
     * 0x29: ADD1 (path address 1);
     * 0x2A: ADD2 (path address 2);
     * 0x2B: ADD3 (path address 3);
     * 0x2C: JOG velocity 2
     * Type: Read/Write
     */
    public static final byte[] DI_6 = new byte[]{0x01, 0x4F};

    /**
     * Digital Input 7: 0x0151
     * Value:
     * 0: invalid;
     * 7: alarm clearing;
     * 8: enable (also can be set by 0x00F);
     * 0x20: Trigger command (CTRG);
     * 0x21: Trigger homing;
     * 0x22: EMG (quick stop);
     * 0x23: JOG+;
     * 0x24: JOG-;
     * 0x25: POT (positive limit);
     * 0x26: NOT (negative limit);
     * 0x27: ORG (home switch);
     * 0x28: ADD0 (path address 0);
     * 0x29: ADD1 (path address 1);
     * 0x2A: ADD2 (path address 2);
     * 0x2B: ADD3 (path address 3);
     * 0x2C: JOG velocity 2
     * Type: Read/Write
     */
    public static final byte[] DI_7 = new byte[]{0x01, 0x51};

    /**
     * Digital Output 1: 0x0157
     * Value:
     * 0: invalid;
     * 0x20: command completed;
     * 0x21: path completed;
     * 0x22: homing completed;
     * 0x23: in-position completed;
     * 0x24: brake output;
     * 0x25: alarm output;
     * Type: Read/Write
     */
    public static final byte[] DO_1 = new byte[]{0x01, 0x57};

    /**
     * Digital Output 2: 0x0159
     * Value:
     * 0: invalid;
     * 0x20: command completed;
     * 0x21: path completed;
     * 0x22: homing completed;
     * 0x23: in-position completed;
     * 0x24: brake output;
     * 0x25: alarm output;
     * Type: Read/Write
     */
    public static final byte[] DO_2 = new byte[]{0x01, 0x59};

    /**
     * Peak Current: 0x0191
     * Value: Approximately equal to motor phase current value multiplied by 1.4
     * Type: Read/Write
     */
    public static final byte[] PEAK_CURRENT = new byte[]{0x01, (byte) 0x91};

    /**
     * Motor Status: 0x1003
     * Value: Bit 0: Fault; Bit 1: Enabled; Bit 2: Running; Bit 4: Completed; Bit 5: Path completed; Bit 6: Homing completed
     * Managed by the MotorStatus class
     * Type: Read
     */
    public static final byte[] MOTOR_STATUS = new byte[]{0x10, 0x03};

    //Todo
    public static final byte[] CONTROL_WORD = new byte[]{0x18, 0x01};

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

}
