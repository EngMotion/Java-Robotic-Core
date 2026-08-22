package com.lucaf.robotic_core.wenglor.ep8e001;

import com.lucaf.robotic_core.impl.SensorInterface;
import com.lucaf.robotic_core.wenglor.impl.DataTypeConversion;
import com.lucaf.robotic_core.wenglor.impl.IndexInterface;

import java.io.IOException;

public class EP8E001IO extends SensorInterface {
    final IndexInterface master;
    final String deviceName;
    final DataTypeConversion conversion = new DataTypeConversion();

    public EP8E001IO(IndexInterface master, String deviceName) {
        this.master = master;
        this.deviceName = deviceName;
    }

    public boolean getInputStatus() throws IOException {
        return master.getProcessGetData(deviceName).isIqValue();
    }

}
