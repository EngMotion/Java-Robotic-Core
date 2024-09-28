package com.lucaf.robotic_core.NANOTEC.PD4E_RTU;

import com.lucaf.robotic_core.Pair;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
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

    private StatusWord getStatusWord() throws DeviceCommunicationException {
        try {
            int statusWord = readRegister(STATUS_WORD);
            return new StatusWord(statusWord);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
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

    private ControlWord operationControl;

    public void start(int operationMode) throws DeviceCommunicationException {
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
                    return statusWord.getStateCode() == StatusWord.States.QUICK_STOP_ACTIVE || statusWord.getStateCode() == StatusWord.States.OPERATION_ENABLED;
                }
            });
            this.operationControl = controlWord;
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    public void stop() throws DeviceCommunicationException {
        try{
            ControlWord controlWord = new ControlWord(0);
            writeRegister(CONTROL_WORD, controlWord.toInt());
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    public void setVelocity(int velocity) throws DeviceCommunicationException {
        if (operationMode != OperationMode.VELOCITY_MODE && operationMode != OperationMode.PROFILE_VELOCITY) throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            writeRegister(TARGET_VELOCITY, velocity);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }
    public int getVelocity() throws DeviceCommunicationException {
        if (operationMode != OperationMode.VELOCITY_MODE && operationMode != OperationMode.PROFILE_VELOCITY) throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            return readRegister(TARGET_VELOCITY);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    int position = 0;

    public void setPositionAbsolute(int position) throws DeviceCommunicationException {
        if (operationMode != OperationMode.PROFILE_POSITION) throw new RuntimeException("Operation mode is not PROFILE_POSITION");
        try {
            position = Math.max(-8388608, Math.min(8388607, position));
            this.position = position;
            writeRegister(TARGET_POSITION, position);
            operationControl.setOperationModeSpecific_4(true);
            operationControl.setOperationModeSpecific_5(true);
            operationControl.setOperationModeSpecific_6(false);
            setControlWordAndWaitForAck(operationControl, new StatusWordCheck() {
                @Override
                public boolean checkStatusWord(StatusWord statusWord) {
                    return statusWord.targetReached;
                }
            });
            operationControl.setOperationModeSpecific_4(false);
            setControlWordAndWaitForAck(operationControl, new StatusWordCheck() {
                @Override
                public boolean checkStatusWord(StatusWord statusWord) {
                    return true;
                }
            });
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    public void setPositionRelative(int position) throws DeviceCommunicationException {
        if (operationMode != OperationMode.PROFILE_POSITION) throw new RuntimeException("Operation mode is not PROFILE_POSITION");
        try {
            position = Math.max(-8388608, Math.min(8388607, position));
            this.position += position;
            writeRegister(TARGET_POSITION, position);
            operationControl.setOperationModeSpecific_4(true);
            operationControl.setOperationModeSpecific_5(true);
            operationControl.setOperationModeSpecific_6(true);
            setControlWordAndWaitForAck(operationControl, new StatusWordCheck() {
                @Override
                public boolean checkStatusWord(StatusWord statusWord) {
                    return statusWord.targetReached;
                }
            });
            operationControl.setOperationModeSpecific_4(false);
            setControlWordAndWaitForAck(operationControl, new StatusWordCheck() {
                @Override
                public boolean checkStatusWord(StatusWord statusWord) {
                    return true;
                }
            });
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    public int getPosition() throws DeviceCommunicationException {
        if (operationMode != OperationMode.VELOCITY_MODE) throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            return readRegister(TARGET_POSITION);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    public void setTorque(int torque) throws DeviceCommunicationException {
        if (operationMode != OperationMode.PROFILE_TORQUE) throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            writeRegister(TARGET_TORQUE, torque);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    public int getTorque() throws DeviceCommunicationException {
        if (operationMode != OperationMode.PROFILE_TORQUE) throw new RuntimeException("Operation mode is not VELOCITY");
        try {
            return readRegister(TARGET_TORQUE);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    public int getMaxCurrent() throws DeviceCommunicationException {
        try {
            return readRegister(MAX_MOTOR_CURRENT);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    public void setMaxCurrent(int current) throws DeviceCommunicationException {
        try {
            writeRegister(MAX_MOTOR_CURRENT, current);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }
    public int getAcceleration() throws DeviceCommunicationException {
        try {
            return readRegister(PROFILE_ACCELERATION);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    public void setAcceleration(int acceleration) throws DeviceCommunicationException {
        try {
            writeRegister(PROFILE_ACCELERATION, acceleration);
        } catch (NanolibHelper.NanolibException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }
}
