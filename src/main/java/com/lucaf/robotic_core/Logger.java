package com.lucaf.robotic_core;

public interface Logger {
    void log(String message);

    void error(String message);

    void warn(String message);

    void debug(String message);
}
