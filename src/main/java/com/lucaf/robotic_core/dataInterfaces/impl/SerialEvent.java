package com.lucaf.robotic_core.dataInterfaces.impl;

import lombok.Getter;

import java.io.IOException;

public abstract class SerialEvent {

    @Getter
    final int type;
    @Getter
    final int value;

    public SerialEvent(int type, int value) {
        this.type = type;
        this.value = value;
    }

    public abstract String readString(int length, long timeoutMillis) throws IOException;
    public abstract byte[] readBytes(int length, long timeoutMillis) throws IOException;
    public abstract String readString() throws IOException;
    public abstract byte[] readBytes() throws IOException;
}
