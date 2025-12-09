package com.lucaf.robotic_core.moxMec;

import lombok.Getter;

public class MoxMecCommand {
    /**
     * If the command is a reply
     */
    private boolean IS_REPLY = false;

    /**
     * If the command was successful
     */
    @Getter
    private boolean SUCCESS = false;

    /**
     * The address of the device
     */
    private char[] ADDRESS = new char[]{'0', '0'};

    /**
     * The command of the device
     */
    private char[] COMMAND = new char[]{'0', '0'};

    /**
     * The value of the command
     */
    private char[] VALUE = new char[]{'0', '0', '0', '0'};

    /**
     * The checksum of the command
     */
    private char[] CHECKSUM = new char[]{'0', '0'};

    /**
     * Method that converts a frame to a string
     *
     * @param frame byte array of the frame
     * @return the string representation of the frame
     */
    public static String frameToString(char[] frame) {
        StringBuilder sb = new StringBuilder();
        for (char c : frame) {
            sb.append(c);
        }
        return sb.toString();
    }

    public MoxMecCommand(char[] frame) {
        if (frame == null || frame.length != 12) {
            throw new IllegalArgumentException("Frame must be 12 characters long");
        }
        IS_REPLY = (frame[0] == 'a' || frame[0] == 'n');
        SUCCESS = (frame[0] == 'a');
        ADDRESS[0] = frame[1];
        ADDRESS[1] = frame[2];
        COMMAND[0] = frame[3];
        COMMAND[1] = frame[4];
        VALUE[0] = frame[5];
        VALUE[1] = frame[6];
        VALUE[2] = frame[7];
        VALUE[3] = frame[8];
        CHECKSUM[0] = frame[9];
        CHECKSUM[1] = frame[10];
    }

    public MoxMecCommand(String frame) {
        this(frame.toCharArray());
    }

    String padZero(String str, int length) {
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < length) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    public MoxMecCommand(int address, int command, int value) {
        this.IS_REPLY = false;
        String addressStr = padZero(Integer.toString(address), 2).substring(0, 2);
        String commandStr = padZero(Integer.toString(command), 2).substring(0, 2);
        String valueStr = padZero(Integer.toString(value), 4).substring(0, 4);
        this.ADDRESS[0] = addressStr.charAt(0);
        this.ADDRESS[1] = addressStr.charAt(1);
        this.COMMAND[0] = commandStr.charAt(0);
        this.COMMAND[1] = commandStr.charAt(1);
        this.VALUE[0] = valueStr.charAt(0);
        this.VALUE[1] = valueStr.charAt(1);
        this.VALUE[2] = valueStr.charAt(2);
        this.VALUE[3] = valueStr.charAt(3);
    }

    public MoxMecCommand(int address) {
        this.IS_REPLY = false;
        String addressStr = padZero(Integer.toString(address), 2).substring(0, 2);
        this.ADDRESS[0] = addressStr.charAt(0);
        this.ADDRESS[1] = addressStr.charAt(1);
    }

    void setCommand(int command) {
        String commandStr = padZero(Integer.toString(command), 2).substring(0, 2);
        this.COMMAND[0] = commandStr.charAt(0);
        this.COMMAND[1] = commandStr.charAt(1);
    }

    void setValue(int value) {
        String valueStr = padZero(Integer.toString(value), 4).substring(0, 4);
        this.VALUE[0] = valueStr.charAt(0);
        this.VALUE[1] = valueStr.charAt(1);
        this.VALUE[2] = valueStr.charAt(2);
        this.VALUE[3] = valueStr.charAt(3);
    }

    public int getCommand() {
        return Integer.parseInt(String.valueOf(COMMAND[0]) + String.valueOf(COMMAND[1]));
    }

    public int getValue() {
        return Integer.parseInt(String.valueOf(VALUE[0]) + String.valueOf(VALUE[1]) + String.valueOf(VALUE[2]) + String.valueOf(VALUE[3]));
    }

    public int getAddress() {
        return Integer.parseInt(String.valueOf(ADDRESS[0]) + String.valueOf(ADDRESS[1]));
    }

    char[] computeChecksum(){
        int sum = 0;
        for (char c : ADDRESS) {
            sum += Integer.parseInt(String.valueOf(c));
        }
        for (char c : COMMAND) {
            sum += Integer.parseInt(String.valueOf(c));
        }
        for (char c : VALUE) {
            sum += Integer.parseInt(String.valueOf(c));
        }
        String checksumStr = padZero(Integer.toString(sum), 2).substring(0, 2);
        CHECKSUM[0] = checksumStr.charAt(0);
        CHECKSUM[1] = checksumStr.charAt(1);
        return CHECKSUM;
    }

    char[] getRequestFrame() {
        computeChecksum();
        char[] frame = new char[12];
        frame[0] = '#';
        frame[1] = ADDRESS[0];
        frame[2] = ADDRESS[1];
        frame[3] = COMMAND[0];
        frame[4] = COMMAND[1];
        frame[5] = VALUE[0];
        frame[6] = VALUE[1];
        frame[7] = VALUE[2];
        frame[8] = VALUE[3];
        frame[9] = CHECKSUM[0];
        frame[10] = CHECKSUM[1];
        frame[11] = 0x0D;
        return frame;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IS_REPLY: ").append(IS_REPLY).append("\n");
        sb.append("SUCCESS: ").append(SUCCESS).append("\n");
        sb.append("ADDRESS: ").append(frameToString(ADDRESS)).append("\n");
        sb.append("COMMAND: ").append(frameToString(COMMAND)).append("\n");
        sb.append("VALUE: ").append(frameToString(VALUE)).append("\n");
        sb.append("CHECKSUM: ").append(frameToString(CHECKSUM)).append("\n");
        return sb.toString();
    }
}
