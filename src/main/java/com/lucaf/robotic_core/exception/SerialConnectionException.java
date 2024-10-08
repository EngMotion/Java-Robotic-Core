package com.lucaf.robotic_core.exception;


/**
 * Exception thrown when a serial connection error occurs.
 */
public class SerialConnectionException extends Exception {

    /**
     * Constructor of the SerialConnectionException class.
     * @param message The message of the exception.
     */
    public SerialConnectionException(String message) {
        super(message);
    }
}
