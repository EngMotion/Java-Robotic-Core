package com.lucaf.robotic_core.DataInterfaces.impl;

import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

public class Register extends SimpleRegister {
    public Register(int v) {
        super(v);
    }

    public Register(short v) {
        super(v);
    }
}
