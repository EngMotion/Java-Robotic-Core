package com.lucaf.robotic_core.exception;

import java.io.IOException;

/**
 * Exception thrown when a communication error occurs.
 */
public class DeviceCommunicationException extends IOException {

    /**
     * Constructor of the DeviceCommunicationException class.
     * @param message The message of the exception.
     */
    public DeviceCommunicationException(String message) {
        super(message);
    }
}
