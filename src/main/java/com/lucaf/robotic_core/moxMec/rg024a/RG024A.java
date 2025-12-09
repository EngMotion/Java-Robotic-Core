package com.lucaf.robotic_core.moxMec.rg024a;

import com.lucaf.robotic_core.SerialParams;
import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.dataInterfaces.impl.SerialInterface;
import com.lucaf.robotic_core.motors.impl.MotorInterface;
import com.lucaf.robotic_core.moxMec.MoxMecCommand;
import com.lucaf.robotic_core.moxMec.MoxMecSerialWrapper;
import com.lucaf.robotic_core.utils.StateUtils;
import jssc.SerialPort;

import java.io.IOException;
import java.util.HashMap;

import static com.lucaf.robotic_core.moxMec.rg024a.Constants.*;

/**
 * This class represents the RG024A device, providing methods to interact with it via a serial interface.
 */
public class RG024A extends MotorInterface {

    public static SerialParams defaultParams = new SerialParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

    /**
     * The low-level serial communication interface to talk with the device.
     */
    final MoxMecSerialWrapper serial;

    /**
     * Shared state map exposed to callers (may contain Atomic* values).
     */
    private final HashMap<String, Object> state;

    /**
     * Optional callback object used to notify state changes and errors.
     */
    private final State stateFunction;

    /**
     * Constructs an RG024A instance with the specified serial interface and address.
     *
     * @param serial  The serial interface used for communication.
     * @param address The address of the RG024A device.
     */
    public RG024A(SerialInterface serial, int address, HashMap<String, Object> state, State stateFunction) {
        this.serial = new MoxMecSerialWrapper(serial, address);
        this.state = state;
        this.stateFunction = stateFunction;
        initState();
    }

    /**
     * Constructs an RG024A instance with the specified serial interface and address.
     *
     * @param serial  The serial interface used for communication.
     * @param address The address of the RG024A device.
     */
    public RG024A(SerialInterface serial, int address) {
        this(serial, address, new HashMap<>(), null);
    }

    /**
     * Notifies the registered State callback (if any) about a state change.
     */
    void notifyStateChange() {
        if (stateFunction != null)
            notifyStateChange();
    }

    /**
     * Initializes and populates the internal and shared state map.
     * Existing values present in the provided state map are respected when possible.
     */
    void initState() {
        state.put("is_moving", isMoving);
        state.put("is_initialized", isInitialized);
        state.put("has_fault", hasError);
        state.put("fault", "");
        notifyStateChange();
    }

    /**
     * Enables the device.
     *
     * @return true if the operation is successful, false otherwise.
     * @throws IOException if there is a communication error.
     */
    public boolean enable() throws IOException {
        isMoving.set(true);
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
        isMoving.set(false);
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

    @Override
    public void stop() throws IOException {
        disable();
        isStopped.set(true);
    }

    @Override
    public void shutdown() throws IOException {
        stop();
        isShutdown.set(true);
        serial.shutdown();
    }
}
