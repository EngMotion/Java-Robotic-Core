package com.lucaf.robotic_core.NANOTEC.PD4E_RTU;

import com.lucaf.robotic_core.Pair;
import com.nanotec.nanolib.DeviceHandle;
import com.nanotec.nanolib.OdIndex;
import com.nanotec.nanolib.helper.NanolibHelper;
import lombok.SneakyThrows;

import static com.lucaf.robotic_core.NANOTEC.PD4E_RTU.Constants.*;
public class PD4E {

    private final NanolibHelper nanolibHelper;
    private final DeviceHandle deviceHandle;
    private StatusWord statusWord = null;

    public PD4E(NanolibHelper nanolibHelper, DeviceHandle deviceHandle) {
        this.nanolibHelper = nanolibHelper;
        this.deviceHandle = deviceHandle;
    }

    private int operationMode = Constants.OperationMode.PROFILE_POSITION;

    private void writeRegister(Pair<OdIndex, Integer> register, int value) throws NanolibHelper.NanolibException {
        nanolibHelper.writeNumber(deviceHandle, value, register.first, register.second);
    }

    private int readRegister(Pair<OdIndex, Integer> register) throws NanolibHelper.NanolibException {
        return (int) nanolibHelper.readNumber(deviceHandle, register.first);
    }

    private StatusWord getStatusWord() {
        try {
            int statusWord = readRegister(STATUS_WORD);
            return new StatusWord(statusWord);
        } catch (NanolibHelper.NanolibException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private void setControlWordAndWaitForAck(ControlWord controlWord, StatusWordCheck statusWordCheck) throws NanolibHelper.NanolibException {
        writeRegister(CONTROL_WORD, controlWord.toInt());
        while (true){
            StatusWord sw = getStatusWord();
            if (statusWordCheck.checkStatusWord(sw)) break;
            Thread.sleep(100);
        }
    }


    public void start(int operationMode) {
        this.operationMode = Math.max(OperationMode.PROFILE_POSITION, Math.min(OperationMode.HOMING, operationMode));
        try {
            writeRegister(MODE_OF_OPERATION,operationMode);
            this.statusWord = getStatusWord();
            if (statusWord.getStateCode() == StatusWord.States.OPERATION_ENABLED) {
                return;
            }
            ControlWord controlWord = new ControlWord();
            controlWord.setQuickStop(true);
            controlWord.setEnableVoltage(true);
            setControlWordAndWaitForAck(controlWord, new StatusWordCheck() {
                @Override
                public boolean checkStatusWord(StatusWord statusWord) {
                    return statusWord.getStateCode() == StatusWord.States.READY_TO_SWITCH_ON;
                }
            });
            controlWord.setSwitchOn(true);
            setControlWordAndWaitForAck(controlWord, new StatusWordCheck() {
                @Override
                public boolean checkStatusWord(StatusWord statusWord) {
                    return statusWord.getStateCode() == StatusWord.States.SWITCHED_ON;
                }
            });
            controlWord.setEnableOperation(true);
            setControlWordAndWaitForAck(controlWord, new StatusWordCheck() {
                @Override
                public boolean checkStatusWord(StatusWord statusWord) {
                    return statusWord.getStateCode() == StatusWord.States.OPERATION_ENABLED;
                }
            });
        } catch (NanolibHelper.NanolibException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop(){
        try{
            ControlWord controlWord = new ControlWord(0);
            writeRegister(CONTROL_WORD, controlWord.toInt());
        } catch (NanolibHelper.NanolibException e) {
            throw new RuntimeException(e);
        }
    }

    public void setVelocity(int velocity){
        if (operationMode != OperationMode.VELOCITY_MODE) throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            writeRegister(TARGET_VELOCITY, velocity);
        } catch (NanolibHelper.NanolibException e) {
            throw new RuntimeException(e);
        }
    }
    public int getVelocity(){
        if (operationMode != OperationMode.VELOCITY_MODE) throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            return readRegister(TARGET_VELOCITY);
        } catch (NanolibHelper.NanolibException e) {
            throw new RuntimeException(e);
        }
    }

    int position = 0;

    public void setPositionAbsolute(int position){
        if (operationMode != OperationMode.PROFILE_POSITION) throw new RuntimeException("Operation mode is not PROFILE_POSITION");
        try {
            position = Math.max(-8388608, Math.min(8388607, position));
            this.position = position;
            writeRegister(TARGET_POSITION, position);
        } catch (NanolibHelper.NanolibException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPositionRelative(int position){
        setPositionAbsolute(this.position + position);
    }

    public int getPosition(){
        if (operationMode != OperationMode.VELOCITY_MODE) throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            return readRegister(TARGET_POSITION);
        } catch (NanolibHelper.NanolibException e) {
            throw new RuntimeException(e);
        }
    }
}
