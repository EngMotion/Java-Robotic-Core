package com.lucaf.robotic_core.wenglor.impl;

public class DataTypeConversion {
    protected String byteArrayToString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            if (b == 0) break;
            sb.append((char) b);
        }
        return sb.toString();
    }

    protected int byteArrayToInt(byte[] data) {
        int value = 0;
        for (int i = 0; i < data.length; i++) {
            value |= (data[i] & 0xFF) << (8 * i);
        }
        return value;
    }

    protected long byteArrayToLong(byte[] data) {
        long value = 0;
        for (int i = 0; i < data.length; i++) {
            value |= (long)(data[i] & 0xFF) << (8 * i);
        }
        return value;
    }

    protected byte[] stringToByteArray(String value, int length) {
        byte[] data = new byte[length];
        byte[] strBytes = value.getBytes();
        for (int i = 0; i < length; i++) {
            if (i < strBytes.length) {
                data[i] = strBytes[i];
            } else {
                data[i] = 0;
            }
        }
        return data;
    }

    protected byte[] intToByteArray(int value, int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) ((value >> (8 * i)) & 0xFF);
        }
        return data;
    }

    protected byte[] longToByteArray(long value, int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) ((value >> (8 * i)) & 0xFF);
        }
        return data;
    }

}
