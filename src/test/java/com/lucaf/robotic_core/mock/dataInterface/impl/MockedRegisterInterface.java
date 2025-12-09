package com.lucaf.robotic_core.mock.dataInterface.impl;

import com.lucaf.robotic_core.dataInterfaces.impl.Register;
import com.lucaf.robotic_core.dataInterfaces.impl.RegisterInterface;
import com.lucaf.robotic_core.Logger;

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
        return writeSingleRegister(registerToInt(register), new Register(data));
    }

    @Override
    public boolean writeSignedLong(byte[] register, long data, boolean invert) throws IOException {
        final long LIMIT = 32768L;
        int data_high = Math.abs((int) (data / LIMIT));
        int data_low = (int) (data % LIMIT);
        if (invert) {
            int temp = data_high;
            data_high = data_low;
            data_low = temp;
        }
        return writeMultipleRegisters(registerToInt(register), new Register[]{new Register(data_high), new Register(data_low)});
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
        final long LIMIT = 32768L;
        return response_high * LIMIT + response_low;
    }

    @Override
    protected void onData(byte[] data) {

    }

    @Override
    protected boolean send(byte[] request) throws IOException {
        return false;
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
