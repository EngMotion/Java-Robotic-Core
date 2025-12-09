package com.lucaf.robotic_core.dataInterfaces.impl;

import com.lucaf.robotic_core.Logger;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public abstract class RegisterInterface extends IOInterface {

    @Getter
    @Setter
    int unitId;

    public RegisterInterface(int unitId, String name, Logger logger) {
        super(name, logger);
        this.unitId = unitId;
    }

    public RegisterInterface(int unitId, String name) {
        super(name);
        this.unitId = unitId;
    }

    abstract public Register[] readMultipleRegisters(int ref, int count) throws IOException;

    abstract public Register readSingleRegister(int ref) throws IOException;

    abstract public boolean writeSingleRegister(int ref, Register register) throws IOException;

    abstract public boolean writeMultipleRegisters(int ref, Register[] registers) throws IOException;

    abstract public boolean writeInteger(byte[] register, int data) throws IOException;

    abstract public boolean writeSignedLong(byte[] register, long data, boolean invert) throws IOException;

    abstract public int readShort(byte[] register) throws IOException;

    abstract public int readInteger(byte[] register) throws IOException;

    abstract public long readSignedLong(byte[] register, boolean invert) throws IOException;
}
