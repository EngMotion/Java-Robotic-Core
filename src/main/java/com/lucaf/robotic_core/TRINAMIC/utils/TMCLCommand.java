package com.lucaf.robotic_core.TRINAMIC.utils;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Class that represents a TMCL command
 */
public class TMCLCommand {

    /**
     * If the command is a reply
     */
    private boolean IS_REPLY = false;

    /**
     * The address of the device
     */
    private byte ADDRESS = 0x01;

    /**
     * The command of the device
     */
    private byte COMMAND = 0x00;

    /**
     * The type of the command
     */
    private byte TYPE = 0x00;

    /**
     * The motor of the command
     */
    private byte MOTOR = 0x00;

    /**
     * The value of the command
     */
    private byte VALUE[] = new byte[]{0x00, 0x00, 0x00, 0x00};

    /**
     * The checksum of the command
     */
    private byte CHECKSUM = 0x00;

    /**
     * The status of the command
     */
    private byte STATUS = 0x00;

    /**
     * The address of the reply
     */
    private byte REPLY_ADDRESS = 0x00;

    /**
     * The statuses of a response command frame
     */
    private static final Map<Integer, String> statuses = Map.ofEntries(
            Map.entry(1, "Wrong Checksum"),
            Map.entry(2, "Invalid Command"),
            Map.entry(3, "Wrong Type"),
            Map.entry(4, "Invalid Value"),
            Map.entry(5, "Configuration EEPROM locked"),
            Map.entry(6, "Command not available"),
            Map.entry(100, "No error"),
            Map.entry(0, "Boh"),
            Map.entry(101, "Command loaded")
    );

    /**
     * Method that converts a frame to a string
     *
     * @param frame byte array of the frame
     * @return the string representation of the frame
     */
    public static String frameToString(byte[] frame) {
        StringBuilder sb = new StringBuilder();
        for (byte b : frame) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    /**
     * Constructor that creates a TMCL command from a response frame
     *
     * @param frame the frame of the response
     */
    public TMCLCommand(byte[] frame) {
        ByteBuffer buffer = ByteBuffer.wrap(frame);
        REPLY_ADDRESS = buffer.get();
        ADDRESS = buffer.get();
        STATUS = buffer.get();
        COMMAND = buffer.get();
        VALUE[0] = buffer.get();
        VALUE[1] = buffer.get();
        VALUE[2] = buffer.get();
        VALUE[3] = buffer.get();
        CHECKSUM = buffer.get();
        IS_REPLY = true;
        //Remove last from buffer
        buffer.position(buffer.position() - 1);
        byte check = crc(frame);
        if (check != CHECKSUM) {
            System.out.println("CRC Error");
        }
    }

    /**
     * Method that checks if the response is ok
     *
     * @return true if the response is ok
     */
    public boolean isOk() {
        return STATUS == 100 || STATUS == 0;
    }

    /**
     * Constructor that creates a TMCL command
     *
     * @param address the address of the device
     * @param motor   the motor of the command
     */
    public TMCLCommand(byte address, byte motor) {
        ADDRESS = address;
        MOTOR = motor;
    }

    /**
     * Constructor that creates a TMCL command with default values
     */
    public TMCLCommand() {
    }

    /**
     * Method that sets the address of the command
     *
     * @param value the address of the command
     */
    public void setValue(int value) {
        //Set value as 4 bytes in VALUE
        VALUE[3] = (byte) (value & 0xFF);
        VALUE[2] = (byte) ((value >> 8) & 0xFF);
        VALUE[1] = (byte) ((value >> 16) & 0xFF);
        VALUE[0] = (byte) ((value >> 24) & 0xFF);
    }

    /**
     * Method that sets the motor of the command
     *
     * @param motor the motor of the command
     */
    public void setMotor(byte motor) {
        MOTOR = motor;
    }

    /**
     * Method that sets the type of the command
     *
     * @param type the type of the command
     */
    public void setType(byte type) {
        TYPE = type;
    }

    /**
     * Method that sets the command of the command
     *
     * @param command the command of the command
     */
    public void setCommand(byte command) {
        COMMAND = command;
    }

    /**
     * Method that sets the address of the command
     *
     * @return the address of the command
     */
    public byte getStatus() {
        return STATUS;
    }

    /**
     * Method that sets the address of the command
     *
     * @return the address of the command
     */
    public byte getReplyAddress() {
        return REPLY_ADDRESS;
    }

    /**
     * Method that gets the value of the command
     *
     * @return the value of the command
     */
    public int getValue() {
        return (VALUE[3] & 0xFF) | ((VALUE[2] & 0xFF) << 8) | ((VALUE[1] & 0xFF) << 16) | ((VALUE[0] & 0xFF) << 24);
    }

    /**
     * Method that gets the motor of the command
     *
     * @return the motor of the command
     */
    public byte getMotor() {
        return MOTOR;
    }

    /**
     * Method that gets the type of the command
     *
     * @return the type of the command
     */
    public byte getType() {
        return TYPE;
    }

    /**
     * Method that gets the command of the command
     *
     * @return the command of the command
     */
    public byte getCommand() {
        return COMMAND;
    }

    /**
     * Method that gets the address of the command
     *
     * @return the address of the command
     */
    public boolean isReply() {
        return IS_REPLY;
    }

    /**
     * Method that calculates the CRC of a frame
     *
     * @param buffer the frame to calculate the CRC
     * @return the CRC of the frame
     */
    private byte crc(byte[] buffer) {
        byte[] data = new byte[8];
        System.arraycopy(buffer, 0, data, 0, 8);
        int crc = 0;
        for (int i = 0; i < data.length; i++) {
            crc += data[i];
        }
        crc = crc % 256;
        return (byte) crc;
    }

    /**
     * Method that gets the frame of the command
     *
     * @return the frame of the command
     */
    public byte[] getFrame() {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.put(ADDRESS);
        buffer.put(COMMAND);
        buffer.put(TYPE);
        buffer.put(MOTOR);
        buffer.put(VALUE);
        CHECKSUM = crc(buffer.array());
        buffer.put(CHECKSUM);
        return buffer.array();
    }

    /**
     * Method that converts the command to a string
     *
     * @return the string representation of the command
     */
    @Override
    public String toString() {
        return "Address: " + ADDRESS + " Command: " + COMMAND + " Type: " + TYPE + " Motor: " + MOTOR + " Value: " + getValue() + " Checksum: " + CHECKSUM + " Status: " + statuses.get((int) STATUS);
    }

}
