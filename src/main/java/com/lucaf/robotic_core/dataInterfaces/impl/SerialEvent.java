package com.lucaf.robotic_core.dataInterfaces.impl;

import java.io.IOException;

public abstract class SerialEvent {
    public abstract String readString(int length, long timeoutMillis) throws IOException;
    public abstract byte[] readBytes(int length, long timeoutMillis) throws IOException;
    public abstract String readString() throws IOException;
    public abstract byte[] readBytes() throws IOException;
}
