package com.lucaf.robotic_core.TRINAMIC.TMCM_3351;

import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.TRINAMIC.USB;
import com.lucaf.robotic_core.TRINAMIC.utils.TMCLCommand;
import com.lucaf.robotic_core.exception.ConfigurationException;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.lucaf.robotic_core.TRINAMIC.TMCM_3351.Constants.*;

/**
 * Class that represents the TMCM_3351 module
 */
@Getter
public class TMCM_3351 {
    /**
     * The USB communication class
     */
    private final USB usb;

    /**
     * The address of the device. Should be 0x01 for the global parameters
     */
    private final byte address = 0x01;

    /**
     * The state of the device
     */
    private final HashMap<String, Object> state;

    /**
     * The state class
     */
    private final State stateFunction;

    /**
     * The class of the constants. Used to get the parameters
     */
    private final Class<?> constantsClass;

    /**
     * Global Logger
     */
    final Logger logger;

    /**
     * Constructor of the class
     *
     * @param com           the USB communication class
     * @param state         the state of the device
     * @param stateFunction the state class, includes the onStateChange method
     * @param logger        the logger
     */
    public TMCM_3351(USB com, HashMap<String, Object> state, State stateFunction, Logger logger) {
        this.state = state;
        this.stateFunction = stateFunction;
        this.usb = com;
        this.logger = logger;
        try {
            constantsClass = Class.forName("com.lucaf.robotic_core.TRINAMIC.TMCM_3351.Constants");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        initState();
    }

    /**
     * Method that initializes the state of the device
     */
    void initState() {
        state.put("global_parameters", new HashMap<String, Integer>());
    }


    /**
     * Method that sets a global parameter
     *
     * @param parameter the parameter to set
     * @param value     the value of the parameter
     * @throws DeviceCommunicationException if there is an error setting the parameter
     */
    private void setGlobalParameter(byte parameter, int value) throws DeviceCommunicationException {
        logger.debug("[TMCM_3351] Setting parameter " + parameter + " to " + value);
        TMCLCommand command = new TMCLCommand(address, (byte) 0x00);
        command.setCommand(SGP);
        command.setValue(value);
        command.setType(parameter);
        TMCLCommand response = usb.write(command);
        if (response == null) {
            throw new DeviceCommunicationException("Error setting parameter");
        }
    }

    /**
     * Method that sets a global parameter
     *
     * @param parameter the parameter to set as a string
     * @param value     the value of the parameter
     */
    private void setGlobalParameter(String parameter, int value) throws ConfigurationException {
        try {
            logger.log("[TMCM_3351] Setting parameter " + parameter + " to " + value);
            Field field = constantsClass.getField(parameter);
            setGlobalParameter((byte) field.get(null), value);
            HashMap<String, Integer> globalParameters = (HashMap<String, Integer>) state.get("global_parameters");
            globalParameters.put(parameter, value);
            stateFunction.notifyStateChange();
        } catch (DeviceCommunicationException | IllegalAccessException | NoSuchFieldException e) {
            throw new ConfigurationException("Parameter not found:" + e.getMessage());
        }

    }

    /**
     * Method that initializes the module with given global parameters
     *
     * @param params the parameters as a map of strings and integers representing the parameter name and value
     * @throws ConfigurationException if there is an error setting the parameters
     */
    public void setGlobalParameters(Map<String, Integer> params) throws ConfigurationException {
        for (Map.Entry<String, Integer> entry : params.entrySet()) {
            setGlobalParameter(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Method that gets the class of a Motor in the module
     *
     * @param motorNumber the number of the motor
     * @param state       the state of the motor
     * @return the motor class
     */
    public TMCM_3351_MOTOR getMotor(byte motorNumber, HashMap<String, Object> state) {
        return new TMCM_3351_MOTOR(this, motorNumber, state, stateFunction, logger);
    }

    /**
     * Method that sets the output of the motor
     *
     * @param port  the port to set
     * @param value the value to set
     * @throws DeviceCommunicationException if there is an error setting the output
     */
    public void setDigitalOutput(int port, boolean value) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, (byte) 2);
        command.setCommand(SIO);
        command.setType((byte) port);
        command.setValue(value ? 1 : 0);
        usb.write(command);
    }

    /**
     * Method that gets the input of the motor
     *
     * @param port the port to get
     * @return the value of the input
     * @throws DeviceCommunicationException if there is an error getting the input
     */
    public int getDigitalInput(int port) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, (byte) 0);
        command.setCommand(GIO);
        command.setType((byte) port);
        TMCLCommand response = usb.write(command);
        return response == null ? -1 : response.getValue();
    }

    /**
     * Method that gets the input of the motor
     *
     * @param port the port to get
     * @return the value of the input
     * @throws DeviceCommunicationException if there is an error getting the input
     */
    public int getAnalogInput(int port) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, (byte) 1);
        command.setCommand(GIO);
        command.setType((byte) port);
        TMCLCommand response = usb.write(command);
        return response == null ? -1 : response.getValue();
    }

    /**
     * Method that restarts the module
     *
     * @throws DeviceCommunicationException if there is an error restarting the module
     */
    public void restart() throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, (byte) 0);
        command.setCommand(RESTART);
        command.setType((byte) 0);
        command.setValue(1234);
        usb.writeAsync(command);
    }


}

