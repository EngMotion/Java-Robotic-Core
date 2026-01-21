package com.lucaf.robotic_core.wenglor.impl;

public class DataTypeConversion {
    public String byteArrayToString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            if (b == 0) break;
            sb.append((char) b);
        }
        return sb.toString();
    }

    public int byteArrayToInt(byte[] data) {
        if (data == null || data.length < 4) {
            throw new IllegalArgumentException("L'array deve contenere almeno 4 byte");
        }

        // Logica BIG-ENDIAN:
        // data[0] è il MSB (Most Significant Byte) -> shift 24
        // data[3] è il LSB (Least Significant Byte) -> shift 0
        return ((data[0] & 0xFF) << 24) |
                ((data[1] & 0xFF) << 16) |
                ((data[2] & 0xFF) << 8)  |
                ((data[3] & 0xFF));
    }

    public long byteArrayToLong(byte[] data) {
        long value = 0;
        for (int i = 0; i < data.length; i++) {
            value |= (long)(data[i] & 0xFF) << (8 * i);
        }
        return value;
    }

    public byte[] stringToByteArray(String value, int length) {
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

    public byte[] intToByteArray(int value, int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) ((value >> (8 * i)) & 0xFF);
        }
        return data;
    }

    public byte[] longToByteArray(long value, int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) ((value >> (8 * i)) & 0xFF);
        }
        return data;
    }

}
