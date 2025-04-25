package com.lucaf.robotic_core.COMMON;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import lombok.Setter;

public class ModbusRTUDevice {

    /**
     * The address id of the device
     * -- SETTER --
     * Public method that sets the address id of the device
     *
     * @param id the address id of the device
     */
    @Setter
    protected byte id = 0x01;


    /**
     * The RS485 connection object
     */
    protected final ModbusSerialMaster rs485;

    /**
     * Global logger class
     */
    protected final Logger logger;

    /**
     * The model of the device
     */
    protected final String model;

    /**
     * Internal method that writes a register
     *
     * @param register the register to write
     * @param data     the data to write
     * @return true if the write is successful, false otherwise
     * @throws DeviceCommunicationException if the write fails
     */
    protected synchronized boolean writeRegister(byte[] register, int data) throws DeviceCommunicationException {
        try {
            logger.debug("[%s] Writing register: " + Integer.toHexString(register[0] << 8 | register[1]) + " with data: " + data);
            int startRegister = register[0] << 8 | register[1];
            int fb = rs485.writeSingleRegister(id, startRegister, new SimpleInputRegister(data));
            logger.debug("[%s] Write response: " + fb);
            return true;
        } catch (ModbusException e) {
            logger.error(String.format("[%s] Write failed: %s", model, e.getMessage()));
            throw new DeviceCommunicationException(e.getMessage());
        }
    }

    /**
     * Internal method that reads a register
     *
     * @param register the register to read
     * @return the response of the device
     * @throws DeviceCommunicationException if the read fails
     */
    protected synchronized int readRegister(byte[] register) throws DeviceCommunicationException {
        try {
            logger.debug(String.format("[%s] Reading register: %s",
                    model,
                    Integer.toHexString(register[0] << 8 | register[1])
            ));
            int startRegister = register[0] << 8 | register[1];
            Register[] regs = rs485.readMultipleRegisters(id, startRegister, 1);
            if (regs != null) {
                logger.debug(String.format("[%s] Read response: %d", model, regs[0].getValue()));
                return regs[0].getValue();
            } else {
                throw new DeviceCommunicationException("No response");
            }
        } catch (ModbusException e) {
            logger.error(String.format("[%s] Read failed: %s", model, e.getMessage()));
            throw new DeviceCommunicationException(e.getMessage());
        }
    }


    /**
     * Internal method that reads a long register
     *
     * @param register_high the register to read
     * @param invert        if true, the response is inverted
     * @return the response of the device
     * @throws DeviceCommunicationException if the read fails
     */
    protected synchronized long readLongRegister(byte[] register_high, boolean invert) throws DeviceCommunicationException {
        try {
            logger.debug(String.format("[%s] Reading long register: %s",
                    model,
                    Integer.toHexString(register_high[0] << 8 | register_high[1])
            ));
            int startRegister = register_high[0] << 8 | register_high[1];
            Register[] regs = rs485.readMultipleRegisters(id, startRegister, 2);
            if (regs != null) {
                int response_high = invert ? regs[1].getValue() : regs[0].getValue();
                int response_low = invert ? regs[0].getValue() : regs[1].getValue();
                int response = (response_high << 16) | response_low;
                logger.debug(String.format("[%s] Read response: %d", model, response));
                return response;
            } else {
                throw new DeviceCommunicationException("No response");
            }
        } catch (ModbusException e) {
            logger.error(String.format("[%s] Read failed: %s", model, e.getMessage()));
            throw new DeviceCommunicationException(e.getMessage());
        }
    }


    /**
     * Internal method that writes a long register
     *
     * @param register_high the high register to write
     * @param data          the data to write
     * @return true if the write is successful, false otherwise
     * @throws DeviceCommunicationException if the write fails
     */
    protected synchronized boolean writeLongRegister(byte[] register_high, long data, boolean inverted) throws DeviceCommunicationException {
        try {
            logger.debug(String.format("[%s] Writing long register: %s with data: %d",
                    model,
                    Integer.toHexString(register_high[0] << 8 | register_high[1]),
                    data
            ));
            int data_high = (int) (0xFFFF & data >> 16);
            int data_low = (int) (0xFFFF & data);
            if (inverted) {
                int temp = data_high;
                data_high = data_low;
                data_low = temp;
            }
            int start_register = register_high[0] << 8 | register_high[1];
            int fb = rs485.writeMultipleRegisters(id, start_register, new Register[]{
                    new SimpleInputRegister(data_high),
                    new SimpleInputRegister(data_low)
            });
            logger.debug(String.format("[%s] Write response: %d", model, fb));
            return true;
        } catch (ModbusException e) {
            logger.error(String.format("[%s] Write failed: %s", model, e.getMessage()));
            throw new DeviceCommunicationException(e.getMessage());
        }
    }


    public ModbusRTUDevice(String model, ModbusSerialMaster rs485, Logger logger) {
        this.model = model;
        this.rs485 = rs485;
        this.logger = logger;
    }

}
