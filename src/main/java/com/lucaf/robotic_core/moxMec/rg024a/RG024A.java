package com.lucaf.robotic_core.moxMec.rg024a;

import com.lucaf.robotic_core.dataInterfaces.impl.SerialInterface;
import com.lucaf.robotic_core.moxMec.MoxMecCommand;
import com.lucaf.robotic_core.moxMec.MoxMecSerialWrapper;

import java.io.IOException;

import static com.lucaf.robotic_core.moxMec.rg024a.Constants.*;

/**
 * This class represents the RG024A device, providing methods to interact with it via a serial interface.
 */
public class RG024A {

    /**
     * The low-level serial communication interface to talk with the device.
     */
    final MoxMecSerialWrapper serial;

    /**
     * Constructs an RG024A instance with the specified serial interface and address.
     *
     * @param serial  The serial interface used for communication.
     * @param address The address of the RG024A device.
     */
    public RG024A(SerialInterface serial, int address) {
        this.serial = new MoxMecSerialWrapper(serial, address);
    }

    /**
     * Enables the device.
     *
     * @return true if the operation is successful, false otherwise.
     * @throws IOException if there is a communication error.
     */
    public boolean enable() throws IOException {
        MoxMecCommand response = serial.sendForResult(new MoxMecCommand(serial.getAddress(), COMMAND_SWITCH, SWITCH_ON));
        return response.isSUCCESS();
    }

    /**
     * Disables the device.
     *
     * @return true if the operation is successful, false otherwise.
     * @throws IOException if there is a communication error.
     */
    public boolean disable() throws IOException {
        MoxMecCommand response = serial.sendForResult(new MoxMecCommand(serial.getAddress(), COMMAND_SWITCH, SWITCH_OFF));
        return response.isSUCCESS();
    }

    /**
     * Toggles the device state between enabled and disabled.
     *
     * @return true if the operation is successful, false otherwise.
     * @throws IOException if there is a communication error.
     */
    public boolean toggle() throws IOException {
        MoxMecCommand response = serial.sendForResult(new MoxMecCommand(serial.getAddress(), COMMAND_SWITCH, SWITCH_TOGGLE));
        return response.isSUCCESS();
    }

    /**
     * Sets the amplitude of the device.
     *
     * @param amplitude The desired amplitude value (0-100).
     * @return true if the operation is successful, false otherwise.
     * @throws IOException if there is a communication error.
     */
    public boolean setAmplitude(int amplitude) throws IOException {
        amplitude = Math.max(0, amplitude);
        amplitude = Math.min(100, amplitude);
        MoxMecCommand response = serial.sendForResult(new MoxMecCommand(serial.getAddress(), COMMAND_SET_AMPLITUDE, amplitude));
        return response.isSUCCESS();
    }

    /**
     * Retrieves the current amplitude of the device.
     *
     * @return The current amplitude value.
     * @throws IOException if there is a communication error.
     */
    public int getAmplitude() throws IOException {
        MoxMecCommand response = serial.sendForResult(new MoxMecCommand(serial.getAddress(), COMMAND_INTERROGATE, READING_AMPLITUDE));
        if (response.isSUCCESS()) {
            return response.getValue();
        } else {
            throw new IOException("Failed to get amplitude");
        }
    }

    /**
     * Sets the frequency of the device.
     *
     * @param frequency The desired frequency value (600-4000).
     * @return true if the operation is successful, false otherwise.
     * @throws IOException if there is a communication error.
     */
    public boolean setFrequency(int frequency) throws IOException {
        frequency = Math.max(600, frequency);
        frequency = Math.min(4000, frequency);
        MoxMecCommand response = serial.sendForResult(new MoxMecCommand(serial.getAddress(), COMMAND_SET_FREQUENCY, frequency));
        return response.isSUCCESS();
    }

    /**
     * Retrieves the current frequency of the device.
     *
     * @return The current frequency value.
     * @throws IOException if there is a communication error.
     */
    public int getFrequency() throws IOException {
        MoxMecCommand response = serial.sendForResult(new MoxMecCommand(serial.getAddress(), COMMAND_INTERROGATE, READING_FREQUENCY));
        if (response.isSUCCESS()) {
            return response.getValue();
        } else {
            throw new IOException("Failed to get frequency");
        }
    }

    /**
     * Sets the ramp value of the device.
     *
     * @param ramp The desired ramp value (0-99).
     * @return true if the operation is successful, false otherwise.
     * @throws IOException if there is a communication error.
     */
    public boolean setRamp(int ramp) throws IOException {
        ramp = Math.max(0, ramp);
        ramp = Math.min(99, ramp);
        MoxMecCommand response = serial.sendForResult(new MoxMecCommand(serial.getAddress(), COMMAND_SET_RAMP, ramp));
        return response.isSUCCESS();
    }

    /**
     * Retrieves the current ramp value of the device.
     *
     * @return The current ramp value.
     * @throws IOException if there is a communication error.
     */
    public int getRamp() throws IOException {
        MoxMecCommand response = serial.sendForResult(new MoxMecCommand(serial.getAddress(), COMMAND_INTERROGATE, READING_RAMP));
        if (response.isSUCCESS()) {
            return response.getValue();
        } else {
            throw new IOException("Failed to get ramp value");
        }
    }

    /**
     * Saves the current configuration of the device.
     *
     * @return true if the operation is successful, false otherwise.
     * @throws IOException if there is a communication error.
     */
    public boolean save() throws IOException {
        MoxMecCommand response = serial.sendForResult(new MoxMecCommand(serial.getAddress(), COMMAND_SAVE, serial.getAddress()));
        return response.isSUCCESS();
    }

    /**
     * Retrieves the current status of the device.
     *
     * @return The current status code of the device.
     * @throws IOException if there is a communication error.
     */
    public StatusCode getStatus() throws IOException {
        MoxMecCommand response = serial.sendForResult(new MoxMecCommand(serial.getAddress(), COMMAND_INTERROGATE, READING_STATUS));
        if (response.isSUCCESS()) {
            return StatusCode.fromValue(response.getValue());
        } else {
            throw new IOException("Failed to get status");
        }
    }

}
