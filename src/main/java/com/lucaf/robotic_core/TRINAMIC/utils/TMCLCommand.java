package com.lucaf.robotic_core.TRINAMIC.utils;

import java.nio.ByteBuffer;

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
        byte check = crc(buffer);
        if(check != CHECKSUM){
            System.out.println("CRC Error");
            //TODO: Check error
        }
    }

    public TMCLCommand(byte address, byte motor){
        ADDRESS = address;
        MOTOR = motor;
    }

    public TMCLCommand(){

    }

    public void setValue(int value){
        VALUE[0] = (byte) (value & 0xFF);
        VALUE[1] = (byte) ((value >> 8) & 0xFF);
        VALUE[2] = (byte) ((value >> 16) & 0xFF);
        VALUE[3] = (byte) ((value >> 24) & 0xFF);
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
        return (VALUE[0] & 0xFF) | ((VALUE[1] & 0xFF) << 8) | ((VALUE[2] & 0xFF) << 16) | ((VALUE[3] & 0xFF) << 24);
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

    private byte crc(ByteBuffer buffer) {
        byte[] data = buffer.array();
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
        CHECKSUM = crc(buffer);
        buffer.put(CHECKSUM);
        return buffer.array();
    }

}
