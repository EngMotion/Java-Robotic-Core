package com.lucaf.robotic_core.DataInterfaces.impl;

import com.lucaf.robotic_core.Logger;
import lombok.Getter;

import java.io.IOException;

public abstract class IOInterface {

    Logger logger;

    @Getter
    final String name;

    public IOInterface(String name, Logger logger) {
        this.logger = logger;
        this.name = name;
    }

    public IOInterface(String name) {
        this.logger = null;
        this.name = name;
    }

    public void logDebug(String message) {
        if (logger != null) {
            logger.debug("[" + name + "] " + message);
        } else {
            System.out.println("[" + name + "] " + message);
        }
    }

    public void logInfo(String message) {
        if (logger != null) {
            logger.log("[" + name + "] " + message);
        } else {
            System.out.println("[" + name + "] " + message);
        }
    }

    public void logError(String message) {
        if (logger != null) {
            logger.error("[" + name + "] " + message);
        } else {
            System.err.println("[" + name + "] " + message);
        }
    }

    public void logWarning(String message) {
        if (logger != null) {
            logger.warn("[" + name + "] " + message);
        } else {
            System.out.println("[" + name + "] " + message);
        }
    }

    protected abstract void onData(byte[] data);

    protected abstract boolean send(byte[] request) throws IOException;

    public abstract boolean isConnected();
}
