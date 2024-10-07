package com.lucaf.robotic_core.TRINAMIC.TMCM_3351;

import com.lucaf.robotic_core.TRINAMIC.USB;
import com.lucaf.robotic_core.TRINAMIC.utils.TMCLCommand;
import com.lucaf.robotic_core.exception.ConfigurationException;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lucaf.robotic_core.TRINAMIC.TMCM_3351.Constants.*;

public class TMCM_3351_MOTOR {
    private final USB usb;
    private final byte motor;
    private final byte address;

    AtomicBoolean isMoving = new AtomicBoolean(false);
    AtomicInteger position = new AtomicInteger(0);

    public TMCM_3351_MOTOR(TMCM_3351 tmcm_3351, byte motorNumber) {
        this.usb = tmcm_3351.getUsb();
        this.address = tmcm_3351.getAddress();
        this.motor = motorNumber;
    }


    /**
     * Method that initializes the module with given parameters
     *
     * @param params the parameters as a map of strings and integers representing the parameter name and value
     * @throws ConfigurationException if there is an error setting the parameters
     */
    public void setParameters(Map<String, Integer> params) throws ConfigurationException {
        try {
            Class<?> constantsClass = Class.forName("com.lucaf.robotic_core.TRINAMIC.TMCM_3351.Constants");
            for (Map.Entry<String, Integer> entry : params.entrySet()) {
                Field field = constantsClass.getField(entry.getKey());
                setParameter((byte) field.get(null), entry.getValue());
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException | IllegalAccessException | DeviceCommunicationException e) {
            throw new ConfigurationException("Parameter not found:" + e.getMessage());
        }

    }

    /**
     * Method that sets a parameter
     *
     * @param parameter the parameter to set
     * @param value     the value of the parameter
     * @throws DeviceCommunicationException if there is an error setting the parameter
     */
    private void setParameter(byte parameter, int value) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(SAP);
        command.setValue(value);
        command.setType(parameter);
        TMCLCommand response = usb.write(command);
        if (response == null) {
            throw new DeviceCommunicationException("Error setting parameter");
        }
    }
    /**
     * Method that rotates the motor to the right
     *
     * @param velocity the velocity of the rotation
     * @throws DeviceCommunicationException if there is an error rotating the motor
     */
    public void rotateRight(int velocity) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(ROR);
        command.setValue(velocity);
        usb.write(command);
    }

    /**
     * Method that rotates the motor to the right
     *
     * @param velocity the velocity of the rotation
     * @throws DeviceCommunicationException if there is an error rotating the motor
     */
    public void rotateLeft(int velocity) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(ROL);
        command.setValue(velocity);
        usb.write(command);
    }

    /**
     * Method that stops the motor
     *
     * @throws DeviceCommunicationException if there is an error stopping the motor
     */
    private void stopMotor() throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(MST);
        usb.write(command);
    }

    /**
     * Method that starts the reference search
     *
     * @param mode the mode of the reference search
     * @throws DeviceCommunicationException if there is an error starting the reference search
     */
    private void startReferenceSearch(byte mode) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(RFS);
        command.setType(mode);
        usb.write(command);
        position.set(0);
    }

    /**
     * Method that moves the motor to the given position
     *
     * @param mode     the mode of the movement
     * @param position the position to move to
     * @throws DeviceCommunicationException if there is an error moving the motor
     */
    private void moveToPosition(byte mode, int position) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(MVP);
        command.setType(mode);
        command.setValue(position);
        usb.write(command);
    }

    /**
     * Method that gets the value of a parameter
     *
     * @param parameter the parameter to get
     * @return the value of the parameter
     * @throws DeviceCommunicationException if there is an error getting the parameter
     */
    private int getParameter(byte parameter) throws DeviceCommunicationException {
        TMCLCommand command = new TMCLCommand(address, motor);
        command.setCommand(GAP);
        command.setType(parameter);
        TMCLCommand response = usb.write(command);
        if (response == null) {
            throw new DeviceCommunicationException("Error getting parameter");
        }
        return response.getValue();
    }

    /**
     * Method that moves the motor to the given position but waits for the movement to end
     *
     * @param mode     the mode of the movement
     * @param position the position to move to
     */
    private void MVP_till_end(byte mode, int position) throws DeviceCommunicationException {
        moveToPosition(mode, position);
        waitTillPositionReached();
    }

    /**
     * Method that waits for the position to be reached. It checkes the position flag wich is 1 if the position is reached
     */
    @SneakyThrows
    private void waitTillPositionReached() throws DeviceCommunicationException {
        while (true) {
            int status = getParameter(PARAM_POSITION_FLAG);
            //1 = reached , 0 = not reached
            if (status == 1) {
                break;
            }
            Thread.sleep(50);
        }
    }

    /**
     * Method that waits for the tasks to end. Used to claim
     */
    @SneakyThrows
    public void waitStatusUnlock() {
        while (true) {
            if (!isMoving.get()) {
                break;
            }
            Thread.sleep(50);
        }
    }

    /**
     * Method that moves the motor to the given position and waits for the movement to end
     *
     * @param position the position to move to
     * @throws DeviceCommunicationException if there is an error moving the motor
     */
    public void moveToRelativePositionAndWait(int position) throws DeviceCommunicationException {
        waitStatusUnlock();
        isMoving.set(true);
        this.position.set(this.position.get() + position);
        MVP_till_end(MVP_REL, position);
        isMoving.set(false);
    }

    /**
     * Method that moves the motor to the given position and waits for the movement to end
     *
     * @param position the position to move to
     * @throws DeviceCommunicationException if there is an error moving the motor
     */
    public void moveToAbsolutePositionAndWait(int position) throws DeviceCommunicationException {
        waitStatusUnlock();
        isMoving.set(true);
        this.position.set(position);
        MVP_till_end(MVP_ABS, position);
        isMoving.set(false);
    }
}
