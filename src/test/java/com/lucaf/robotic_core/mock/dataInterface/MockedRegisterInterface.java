package com.lucaf.robotic_core.mock.dataInterface;

import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.lucaf.robotic_core.dataInterfaces.impl.RegisterInterface;
import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.dataInterfaces.modbus.ModbusRS485;

import java.io.IOException;
import java.util.HashMap;

public class MockedRegisterInterface extends RegisterInterface {
    final HashMap<Integer, Register> registerMap = new HashMap<>();
    final HashMap<Integer, Register[]> registerArrayMap = new HashMap<>();

    public MockedRegisterInterface(int unitId, String name, Logger logger) {
        super(unitId, name, logger);
    }

    public MockedRegisterInterface(int unitId, String name) {
        super(unitId, name);
    }

    @Override
    public Register[] readMultipleRegisters(int ref, int count) throws IOException {
        return registerArrayMap.get(ref);
    }

    @Override
    public Register readSingleRegister(int ref) throws IOException {
        return registerMap.get(ref);
    }

    @Override
    public boolean writeSingleRegister(int ref, Register register) throws IOException {
        registerMap.put(ref, register);
        return true;
    }

    @Override
    public boolean writeMultipleRegisters(int ref, Register[] registers) throws IOException {
        registerArrayMap.put(ref, registers);
        return true;
    }

    int registerToInt(byte[] register) {
        int value = 0;
        for (int i = 0; i < register.length; i++) {
            value = (value << 8) | (register[i] & 0xFF);
        }
        return value;
    }

    @Override
    public boolean writeInteger(byte[] register, int data) throws IOException {
        return writeSingleRegister(registerToInt(register), new SimpleRegister(data));
    }

    @Override
    public boolean writeSignedLong(byte[] register, long data, boolean invert) throws IOException {
        int data_low = Math.toIntExact(data & 0xFFFF);
        int data_high = Math.toIntExact((data >> 16) & 0xFFFF);
        System.out.println("Writing long to register " + registerToInt(register) + ": high=" + data_high + ", low=" + data_low + ", invert=" + invert);
        if (invert) {
            int temp = data_high;
            data_high = data_low;
            data_low = temp;
        }
        return writeMultipleRegisters(registerToInt(register), new Register[]{new SimpleRegister(data_high), new SimpleRegister(data_low)});
    }

    @Override
    public int readShort(byte[] register) throws IOException {
        return readSingleRegister(registerToInt(register)).toShort();
    }

    @Override
    public int readInteger(byte[] register) throws IOException {
        return readSingleRegister(registerToInt(register)).getValue();
    }

    @Override
    public long readSignedLong(byte[] register, boolean invert) throws IOException {
        Register[] regs = readMultipleRegisters(registerToInt(register), 2);
        int response_high = invert ? regs[1].getValue() : regs[0].getValue();
        int response_low = invert ? regs[0].toShort() : regs[1].toShort();

        System.out.println("Reading long from register " + registerToInt(register) + ": high=" + response_high + ", low=" + response_low + ", invert=" + invert);

        return ModbusRS485.reconstructSigned32Bit(response_high, response_low);
    }

    @Override
    protected void onData(byte[] data) {

    }

    @Override
    public boolean send(byte[] request) throws IOException {
        return false;
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
