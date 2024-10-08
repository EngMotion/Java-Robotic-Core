package com.lucaf.robotic_core.exception;

/**
 * Exception thrown when a communication error occurs.
 */
public class DeviceCommunicationException extends Exception {

    /**
     * Constructor of the DeviceCommunicationException class.
     * @param message The message of the exception.
     */
    public DeviceCommunicationException(String message) {
        super(message);
    }
}
