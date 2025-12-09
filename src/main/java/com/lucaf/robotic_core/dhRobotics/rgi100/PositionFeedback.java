package com.lucaf.robotic_core.dhRobotics.rgi100;

/**
 * Enum that defines the possible position feedback states returned by the device.
 */
public enum PositionFeedback {
    /**
     * The device is still moving toward the target.
     */
    MOVING,
    /**
     * The rotator reached the requested position.
     */
    REACHED,
    /**
     * The rotator encountered a blockage while moving.
     */
    BLOCKED,
    /**
     * The grip reached its target and detected an object.
     */
    REACHED_WITH_OBJ,
    /**
     * The grip reached its target and did not detect an object.
     */
    REACHED_WITHOUT_OBJ,
    /**
     * The grip experienced a fall/drop event.
     */
    FALL,
    /**
     * An unspecified or communication error occurred while moving.
     */
    ERROR
}