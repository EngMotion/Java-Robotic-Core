package com.lucaf.robotic_core.exception;

/**
 * Exception thrown when a configuration error occurs.
 */
public class ConfigurationException extends Exception{
    /**
     * Constructor of the ConfigurationException class.
     * @param message The message of the exception.
     */
    public ConfigurationException(String message) {
        super(message);
    }
}
