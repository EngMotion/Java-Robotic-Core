package com.lucaf.robotic_core.dataInterfaces.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.AbstractModbusMaster;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.lucaf.robotic_core.dataInterfaces.impl.Register;
import com.lucaf.robotic_core.dataInterfaces.impl.RegisterInterface;
import com.lucaf.robotic_core.Logger;

import java.io.IOException;

public class ModbusRS485 extends RegisterInterface {

    /**
     * The RS485 modbus connection object
     */
    private final AbstractModbusMaster rs485;

    public ModbusRS485(AbstractModbusMaster rs485, int unitId, String name) {
        super(unitId, name);
        this.rs485 = rs485;
    }

    public ModbusRS485(AbstractModbusMaster rs485, int unitId, String name, Logger logger) {
        super(unitId, name, logger);
        this.rs485 = rs485;
    }

    @Override
    public Register[] readMultipleRegisters(int ref, int count) throws IOException {
        try {
            Register[] registers = (Register[]) rs485.readMultipleRegisters(getUnitId(), ref, count);
            if (registers.length == count) {
                return registers;
            } else {
                throw new IOException("Insufficient registers returned");
            }
        } catch (ModbusException e) {
            throw new IOException("Failed to read multiple registers", e);
        }
    }

    @Override
    public Register readSingleRegister(int ref) throws IOException {
        try {
            Register[] registers = (Register[]) rs485.readMultipleRegisters(getUnitId(), ref, 1);
            if (registers.length > 0) {
                return registers[0];
            } else {
                throw new IOException("No registers returned");
            }
        } catch (ModbusException e) {
            throw new IOException("Failed to read single register", e);
        }
    }

    @Override
    public boolean writeSingleRegister(int ref, Register register) throws IOException {
        try {
            return rs485.writeSingleRegister(getUnitId(), ref, register) == register.getValue();
        } catch (ModbusException e) {
            throw new IOException("Failed to write single register", e);
        }
    }

    @Override
    public boolean writeMultipleRegisters(int ref, Register[] registers) throws IOException {
        try {
            return rs485.writeMultipleRegisters(getUnitId(), ref, registers) == registers.length;
        } catch (ModbusException e) {
            throw new IOException("Failed to write multiple registers", e);
        }
    }

    @Override
    public boolean writeInteger(byte[] register, int data) throws IOException {
        try {
            logDebug(String.format("Writing register: 0x%02X - 0x%02X with data: %s",
                    register[0] & 0xFF,
                    register[1] & 0xFF,
                    data
            ));
            int startRegister = register[0] << 8 | register[1];
            boolean fb = writeSingleRegister(startRegister, new Register(data));
            logDebug(String.format("Write response: 0x%02X - 0x%02X = %s",
                    register[0] & 0xFF,
                    register[1] & 0xFF,
                    fb
            ));
            return true;
        } catch (IOException e) {
            logError(String.format("Writing register failed: 0x%02X - 0x%02X with data: %s - %s",
                    register[0] & 0xFF,
                    register[1] & 0xFF,
                    data,
                    e.getMessage()
            ));
            throw e;
        }
    }

    @Override
    public boolean writeSignedLong(byte[] register, long data, boolean inverted) throws IOException {
        try {
            logDebug(String.format("Writing long register: 0x%02X - 0x%02X with data: %s (inverted: %s)",
                    register[0] & 0xFF,
                    register[1] & 0xFF,
                    data,
                    inverted
            ));
            final long LIMIT = 32768L;
            int data_high = Math.abs((int) (data / LIMIT));
            int data_low = (int) (data % LIMIT);
            if (inverted) {
                int temp = data_high;
                data_high = data_low;
                data_low = temp;
            }
            int start_register = register[0] << 8 | register[1];
            boolean success = writeMultipleRegisters(start_register, new Register[]{
                    new Register(data_high),
                    new Register(data_low)
            });
            if (success) {
                logDebug(String.format("Write response success: 0x%02X - 0x%02X",
                        register[0] & 0xFF,
                        register[1] & 0xFF
                ));
                return true;
            } else {
                logError(String.format("Writing long register failed: 0x%02X - 0x%02X with data: %s - No success response",
                        register[0] & 0xFF,
                        register[1] & 0xFF,
                        data
                ));
                throw new IOException("No success response");
            }
        } catch (IOException e) {
            logError(String.format("Writing long register failed: 0x%02X - 0x%02X with data: %s - %s",
                    register[0] & 0xFF,
                    register[1] & 0xFF,
                    data,
                    e.getMessage()
            ));
            throw e;
        }
    }

    @Override
    public int readShort(byte[] register) throws IOException {
        try {
            logDebug(String.format("Reading register: 0x%02X - 0x%02X",
                    register[0] & 0xFF,
                    register[1] & 0xFF
            ));
            int startRegister = register[0] << 8 | register[1];
            Register regs = readSingleRegister(startRegister);
            logDebug(String.format("Read response: 0x%02X - 0x%02X = %d",
                    register[0] & 0xFF,
                    register[1] & 0xFF,
                    regs.toShort()
            ));
            return regs.toShort();
        } catch (IOException e) {
            logError(String.format("Reading register failed: 0x%02X - 0x%02X - %s",
                    register[0] & 0xFF,
                    register[1] & 0xFF,
                    e.getMessage()
            ));
            throw e;
        }
    }

    @Override
    public int readInteger(byte[] register) throws IOException {
        try {
            logDebug(String.format("Reading unsigned register: 0x%02X - 0x%02X",
                    register[0] & 0xFF,
                    register[1] & 0xFF
            ));
            int startRegister = register[0] << 8 | register[1];
            Register regs = readSingleRegister(startRegister);
            logDebug(String.format("Read response: 0x%02X - 0x%02X = %d",
                    register[0] & 0xFF,
                    register[1] & 0xFF,
                    regs.getValue()
            ));
            return regs.getValue();
        } catch (IOException e) {
            logError(String.format("Reading register failed: 0x%02X - 0x%02X - %s",
                    register[0] & 0xFF,
                    register[1] & 0xFF,
                    e.getMessage()
            ));
            throw e;
        }
    }

    @Override
    public long readSignedLong(byte[] register, boolean invert) throws IOException {
        try {
            logDebug(String.format("Reading long register: 0x%02X - 0x%02X",
                    register[0] & 0xFF,
                    register[1] & 0xFF
            ));
            int startRegister = register[0] << 8 | register[1];
            Register[] regs = readMultipleRegisters(startRegister, 2);
            int response_high = invert ? regs[1].getValue() : regs[0].getValue();
            int response_low = invert ? regs[0].toShort() : regs[1].toShort();
            final long LIMIT = 32768L;
            long response = response_high * LIMIT + response_low;
            logDebug(String.format("Read response: 0x%02X - 0x%02X = %d",
                    register[0] & 0xFF,
                    register[1] & 0xFF,
                    response
            ));
            return response;
        } catch (IOException e) {
            logError(String.format("Reading long register failed: 0x%02X - 0x%02X - %s",
                    register[0] & 0xFF,
                    register[1] & 0xFF,
                    e.getMessage()
            ));
            throw e;
        }
    }

    @Override
    protected void onData(byte[] data) {
        // Not implemented for Modbus RS485
    }

    @Override
    public boolean send(byte[] request) throws IOException {
        // Not implemented for Modbus RS485
        return false;
    }

    @Override
    public boolean isConnected() {
        return rs485.isConnected();
    }

    /**
     * Method that checks if the device is connected
     *
     * @param rs485 the RS485 connection object
     * @param id    the address id of the device
     * @return true if the device is connected, false otherwise
     */
    public static boolean ping(ModbusSerialMaster rs485, int id) {
        try {
            com.ghgande.j2mod.modbus.procimg.Register[] regs = rs485.readMultipleRegisters(id, 0, 1);
            return regs != null;
        } catch (ModbusException e) {
            return false;
        }
    }
}
