package com.lucaf.robotic_core.STEPPERONLINE.iDM_RS;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.Constants.*;
public class iDM_RS {
    final ModbusSerialMaster rs485;
    final HashMap<String, Object> state;
    final ControlMode controlMode = new ControlMode(0x00);
    final State notifyStateChange;
    final byte id;
    final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public iDM_RS(ModbusSerialMaster rs485, byte id, HashMap<String, Object> state, State notifyStateChange){
        this.rs485 = rs485;
        this.state = state;
        this.notifyStateChange = notifyStateChange;
        this.id = id;
        initState();
    }

    private void initState(){
        state.put("speed", 0);
        state.put("acceleration", 0);
        state.put("deceleration", 0);
        state.put("position", 0L);
    }

    /**
     * Internal method that reads a register
     *
     * @param register the register to read
     * @return the response of the device
     */
    private synchronized int readRegister(byte[] register) throws DeviceCommunicationException {
        try {
            int startRegister = register[0] << 8 | register[1];
            Register[] regs = rs485.readMultipleRegisters(id, startRegister, 1);
            if (regs != null) {
                return regs[0].getValue();
            }
            return -1;
        } catch (ModbusException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Internal method that writes a register
     *
     * @param register the register to write
     * @param data     the data to write
     * @return true if the write is successful, false otherwise
     */
    private synchronized boolean writeRegister(byte[] register, int data) throws DeviceCommunicationException {
        try {
            int startRegister = register[0] << 8 | register[1];
            rs485.writeSingleRegister(id, startRegister, new SimpleInputRegister(data));
            return true;
        } catch (ModbusException e) {
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    public void setSpeed(int speed) throws DeviceCommunicationException {
        writeRegister(VELOCITY, speed);
        state.put("speed", speed);
    }

    public int getSpeed() throws DeviceCommunicationException {
        int speed = readRegister(VELOCITY);
        state.put("speed", speed);
        return speed;
    }

    public void stop() throws DeviceCommunicationException {
        writeRegister(STATUS_MODE,StatusMode.getEMERGENCY_STOP());
    }

    public void setAcceleration(int acceleration) throws DeviceCommunicationException {
        writeRegister(ACCELERATION, acceleration);
        state.put("acceleration", acceleration);
    }

    public int getAcceleration() throws DeviceCommunicationException {
        int acceleration = readRegister(ACCELERATION);
        state.put("acceleration", acceleration);
        return acceleration;
    }

    public void setDeceleration(int deceleration) throws DeviceCommunicationException {
        writeRegister(DECELERATION, deceleration);
        state.put("deceleration", deceleration);
    }

    public int getDeceleration() throws DeviceCommunicationException {
        int deceleration = readRegister(DECELERATION);
        state.put("deceleration", deceleration);
        return deceleration;
    }

    public void setPosition(long position) throws DeviceCommunicationException {
        if (controlMode.isRELATIVE_POSITIONING()){
            state.put("position", (long) state.get("position") + position);
        }else{
            state.put("position", position);
        }
        int position_high = (int) (position >> 32);
        int position_low = (int) (position & 0xFFFFFFFF);
        writeRegister(TARGET_POSITION_HIGH, position_high);
        writeRegister(TARGET_POSITION_LOW, position_low);
        writeRegister(STATUS_MODE, StatusMode.getSegmentPositioning((byte) 0x00));
    }

    public void writeControlMode() throws DeviceCommunicationException {
        writeRegister(CONTROL_MODE, controlMode.toInt());
    }

    public void setPositioningMode() throws DeviceCommunicationException {
        controlMode.setCONTROL_MODE(1);
        writeControlMode();
    }

    public void setVelocityMode() throws DeviceCommunicationException {
        controlMode.setCONTROL_MODE(2);
        writeControlMode();
    }

    public StatusMode getStatusMode() throws DeviceCommunicationException {
        return new StatusMode(readRegister(STATUS_MODE));
    }

    public void setRelativePositioning(boolean relative) throws DeviceCommunicationException {
        controlMode.setRELATIVE_POSITIONING(relative);
        writeControlMode();
    }

    @SneakyThrows
    public void waitReachedPosition() throws DeviceCommunicationException {
        while (true){
            StatusMode mode = getStatusMode();
            if (!mode.isRunning()) break;
            Thread.sleep(50);
        }
    }

    public Future<Boolean> moveToPostionAndWait(int position){
        return executorService.submit(() -> {
            try {
                setPosition(position);
                waitReachedPosition();
                return true;
            }catch (Exception e){
                return false;
            }
        });
    }
}
