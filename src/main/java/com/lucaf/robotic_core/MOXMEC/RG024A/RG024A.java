package com.lucaf.robotic_core.MOXMEC.RG024A;

import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.MOXMEC.MoxMecCommand;
import com.lucaf.robotic_core.MOXMEC.Serial;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import lombok.Setter;

import static com.lucaf.robotic_core.MOXMEC.RG024A.Constants.*;

public class RG024A {

    @Setter
    int address = 1;

    final Serial serial;
    final Logger logger;


    public RG024A(Serial serial, Logger logger) {
        this.serial = serial;
        this.logger = logger;
    }

    public boolean enable() throws DeviceCommunicationException {
        MoxMecCommand response = serial.write(new MoxMecCommand(address, COMMAND_SWITCH, SWITCH_ON));
        return response.isSUCCESS();
    }
    public boolean disable() throws DeviceCommunicationException {
        MoxMecCommand response = serial.write(new MoxMecCommand(address, COMMAND_SWITCH, SWITCH_OFF));
        return response.isSUCCESS();
    }
    public boolean toggle() throws DeviceCommunicationException {
        MoxMecCommand response = serial.write(new MoxMecCommand(address, COMMAND_SWITCH, SWITCH_TOGGLE));
        return response.isSUCCESS();
    }
    public boolean setAmplitude(int amplitude) throws DeviceCommunicationException {
        if (amplitude < 0 || amplitude > 100) {
            throw new IllegalArgumentException("Amplitude must be between 0 and 100");
        }
        MoxMecCommand response = serial.write(new MoxMecCommand(address, COMMAND_SET_AMPLITUDE, amplitude));
        return response.isSUCCESS();
    }
    public int getAmplitude() throws DeviceCommunicationException {
        MoxMecCommand response = serial.write(new MoxMecCommand(address, COMMAND_INTERROGATE, READING_AMPLITUDE));
        if (response.isSUCCESS()) {
            return response.getValue();
        } else {
            throw new DeviceCommunicationException("Failed to get amplitude");
        }
    }
    public boolean setFrequency(int frequency) throws DeviceCommunicationException {
        if (frequency < 600 || frequency > 4000) {
            throw new IllegalArgumentException("Frequency must be between 600 and 4000");
        }
        MoxMecCommand response = serial.write(new MoxMecCommand(address, COMMAND_SET_FREQUENCY, frequency));
        return response.isSUCCESS();
    }
    public int getFrequency() throws DeviceCommunicationException {
        MoxMecCommand response = serial.write(new MoxMecCommand(address, COMMAND_INTERROGATE, READING_FREQUENCY));
        if (response.isSUCCESS()) {
            return response.getValue();
        } else {
            throw new DeviceCommunicationException("Failed to get frequency");
        }
    }
    public boolean setRamp(int ramp) throws DeviceCommunicationException {
        if (ramp < 0 || ramp > 99) {
            throw new IllegalArgumentException("Ramp must be between 0 and 99");
        }
        MoxMecCommand response = serial.write(new MoxMecCommand(address, COMMAND_SET_RAMP, ramp));
        return response.isSUCCESS();
    }
    public int getRamp() throws DeviceCommunicationException {
        MoxMecCommand response = serial.write(new MoxMecCommand(address, COMMAND_INTERROGATE, READING_RAMP));
        if (response.isSUCCESS()) {
            return response.getValue();
        } else {
            throw new DeviceCommunicationException("Failed to get ramp");
        }
    }
    public boolean save() throws DeviceCommunicationException {
        MoxMecCommand response = serial.write(new MoxMecCommand(address, COMMAND_SAVE, address));
        return response.isSUCCESS();
    }
    public Constants.StatusCode getStatus() throws DeviceCommunicationException {
        MoxMecCommand response = serial.write(new MoxMecCommand(address, COMMAND_INTERROGATE, READING_STATUS));
        if (response.isSUCCESS()) {
            return Constants.StatusCode.fromValue(response.getValue());
        } else {
            throw new DeviceCommunicationException("Failed to get status");
        }
    }

}
