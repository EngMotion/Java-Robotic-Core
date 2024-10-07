package com.lucaf.robotic_core.TRINAMIC.utils;

import java.nio.ByteBuffer;
import java.util.Map;

public class TMCLCommand {

    private boolean IS_REPLY = false;

    private byte ADDRESS = 0x01;
    private byte COMMAND = 0x00;
    private byte TYPE = 0x00;
    private byte MOTOR = 0x00;
    private byte VALUE[] = new byte[]{0x00,0x00,0x00,0x00};
    private byte CHECKSUM = 0x00;

    private byte STATUS = 0x00;
    private byte REPLY_ADDRESS = 0x00;

    private static final Map<Integer,String> statuses = Map.ofEntries(
            Map.entry(1,"Wrong Checksum"),
            Map.entry(2,"Invalid Command"),
            Map.entry(3,"Wrong Type"),
            Map.entry(4,"Invalid Value"),
            Map.entry(5,"Configuration EEPROM locked"),
            Map.entry(6,"Command not available"),
            Map.entry(100,"No error"),
            Map.entry(101,"Command loaded")
    );

    public static String frameToString (byte[] frame){
        StringBuilder sb = new StringBuilder();
        for (byte b : frame) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

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
        buffer.position(buffer.position()-1);
        byte check = crc(frame);
        if(check != CHECKSUM){
            System.out.println("CRC Error");
        }
    }

    public boolean isOk(){
        return STATUS == 100;
    }

    public TMCLCommand(byte address, byte motor){
        ADDRESS = address;
        MOTOR = motor;
    }

    public TMCLCommand(){

    }

    public void setValue(int value){
        //Set value as 4 bytes in VALUE
        VALUE[3] = (byte) (value & 0xFF);
        VALUE[2] = (byte) ((value >> 8) & 0xFF);
        VALUE[1] = (byte) ((value >> 16) & 0xFF);
        VALUE[0] = (byte) ((value >> 24) & 0xFF);
    }

    public void setMotor(byte motor){
        MOTOR = motor;
    }

    public void setType(byte type){
        TYPE = type;
    }

    public void setCommand(byte command){
        COMMAND = command;
    }

    public byte getStatus(){
        return STATUS;
    }

    public byte getReplyAddress(){
        return REPLY_ADDRESS;
    }

    public int getValue(){
        return (VALUE[3] & 0xFF) | ((VALUE[2] & 0xFF) << 8) | ((VALUE[1] & 0xFF) << 16) | ((VALUE[0] & 0xFF) << 24);
    }

    public byte getMotor(){
        return MOTOR;
    }

    public byte getType(){
        return TYPE;
    }

    public byte getCommand(){
        return COMMAND;
    }

    public boolean isReply(){
        return IS_REPLY;
    }

    private byte crc( byte[] buffer ) {
        byte[] data = new byte[8];
        System.arraycopy(buffer, 0, data, 0, 8);
        int crc = 0;
        for (int i = 0; i < data.length; i++) {
            crc += data[i];
        }
        crc = crc % 256;
        return (byte) crc;
    }

    public byte[] getFrame(){
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

    @Override
    public String toString(){
        return "Address: " + ADDRESS + " Command: " + COMMAND + " Type: " + TYPE + " Motor: " + MOTOR + " Value: " + getValue() + " Checksum: " + CHECKSUM + " Status: " + statuses.get((int) STATUS);
    }

}
