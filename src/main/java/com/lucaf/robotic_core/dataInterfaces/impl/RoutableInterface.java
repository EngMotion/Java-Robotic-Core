package com.lucaf.robotic_core.dataInterfaces.impl;

import com.lucaf.robotic_core.Logger;
import okhttp3.MediaType;

import java.io.IOException;

public abstract class RoutableInterface extends IOInterface {
    public RoutableInterface(String name, Logger logger) {
        super(name, logger);
    }

    public abstract int getMasterID() throws IOException;

    public abstract byte[] get(String address) throws IOException;

    public abstract byte[] post(String address, byte[] data, MediaType mediaType) throws IOException;

    public abstract byte[] put(String address, byte[] data, MediaType mediaType) throws IOException;
    public abstract byte[] delete(String address) throws IOException;
}
