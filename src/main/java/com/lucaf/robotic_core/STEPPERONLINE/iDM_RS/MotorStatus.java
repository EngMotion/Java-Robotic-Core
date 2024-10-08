package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

import lombok.Getter;

/**
 * Class that represents the status of a motor.
 */
@Getter
public class MotorStatus {

    /**
     * Bit 0: Fault
     */
    boolean fault;

    /**
     * Bit 1: Enabled
     */
    boolean enabled;

    /**
     * Bit 2: Running
     */
    boolean running;

    /**
     * Bit 4: Completed
     */
    boolean completed;

    /**
     * Bit 5: Path completed
     */
    boolean pathCompleted;

    /**
     * Bit 6: Homing completed
     */
    boolean homingCompleted;

    /**
     * Constructor of the MotorStatus class.
     * @param fault
     * @param enabled
     * @param running
     * @param completed
     * @param pathCompleted
     * @param homingCompleted
     */
    public MotorStatus(boolean fault, boolean enabled, boolean running, boolean completed, boolean pathCompleted, boolean homingCompleted) {
        this.fault = fault;
        this.enabled = enabled;
        this.running = running;
        this.completed = completed;
        this.pathCompleted = pathCompleted;
        this.homingCompleted = homingCompleted;
    }

    /**
     * Constructor of the MotorStatus class.
     * @param status The status of the motor.
     */
    public MotorStatus(int status) {
        this.fault = (status & 0x01) != 0;
        this.enabled = (status & 0x02) != 0;
        this.running = (status & 0x04) != 0;
        this.completed = (status & 0x10) != 0;
        this.pathCompleted = (status & 0x20) != 0;
        this.homingCompleted = (status & 0x40) != 0;
    }

    /**
     * Method that returns the status of the motor as an integer.
     * @return The status of the motor as an integer.
     */
    public int toInt() {
        int status = 0;
        status = fault ? status | 0x01 : status;
        status = enabled ? status | 0x02 : status;
        status = running ? status | 0x04 : status;
        status = completed ? status | 0x10 : status;
        status = pathCompleted ? status | 0x20 : status;
        status = homingCompleted ? status | 0x40 : status;
        return status;
    }

    /**
     * Method that returns the status of the motor as a string.
     * @return The status of the motor as a string.
     */
    @Override
    public String toString() {
        return "MotorStatus{" +
                "fault=" + fault +
                ", enabled=" + enabled +
                ", running=" + running +
                ", completed=" + completed +
                ", pathCompleted=" + pathCompleted +
                ", homingCompleted=" + homingCompleted +
                '}';
    }
}
