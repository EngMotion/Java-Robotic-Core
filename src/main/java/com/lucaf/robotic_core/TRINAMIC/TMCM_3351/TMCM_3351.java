package com.lucaf.robotic_core.TRINAMIC.TMCM_3351;

import com.lucaf.robotic_core.TRINAMIC.USB;
import com.lucaf.robotic_core.TRINAMIC.utils.TMCLCommand;
import com.lucaf.robotic_core.exception.ConfigurationException;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Map;

import static com.lucaf.robotic_core.TRINAMIC.TMCM_3351.Constants.*;

public class TMCM_3351 {
    @Getter
    private final USB usb;
    @Getter
    private final byte address = 0x01;

    public TMCM_3351(USB com) {
        this.usb = com;
    }

    /**
     * Method that sets a global parameter
     *
     * @param parameter the parameter to set
     * @param value     the value of the parameter
     * @throws DeviceCommunicationException if there is an error setting the parameter
     */
    private void setGlobalParameter(byte parameter, int value) throws DeviceCommunicationException {
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
     * Method that initializes the module with given global parameters
     *
     * @param params the parameters as a map of strings and integers representing the parameter name and value
     * @throws ConfigurationException if there is an error setting the parameters
     */
    public void setGlobalParameters(Map<String, Integer> params) throws ConfigurationException {
        try {
            Class<?> constantsClass = Class.forName("com.lucaf.robotic_core.TRINAMIC.TMCM_3351.Constants");
            for (Map.Entry<String, Integer> entry : params.entrySet()) {
                Field field = constantsClass.getField(entry.getKey());
                setGlobalParameter((byte) field.get(null), entry.getValue());
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException | IllegalAccessException | DeviceCommunicationException e) {
            throw new ConfigurationException("Parameter not found:" + e.getMessage());
        }
    }

    public TMCM_3351_MOTOR getMotor(byte motorNumber) {
        return new TMCM_3351_MOTOR(this, motorNumber);
    }
}
