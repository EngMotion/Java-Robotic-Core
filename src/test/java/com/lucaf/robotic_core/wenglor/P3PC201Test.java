package com.lucaf.robotic_core.wenglor;

import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.dataInterfaces.tcp.HttpJsonConnector;
import com.lucaf.robotic_core.wenglor.ep8e001.EP8E001;
import com.lucaf.robotic_core.wenglor.p3pc201.P3PC201;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class P3PC201Test {
    EP8E001 master;
    P3PC201 sensor;
    @BeforeEach
    public void setup() {
        HttpJsonConnector client = new HttpJsonConnector("D:X", new Logger() {
            @Override
            public void log(String message) {
                System.out.println(message);
            }

            @Override
            public void error(String message) {
                System.err.println(message);
            }

            @Override
            public void warn(String message) {
                System.out.println("WARN: " + message);
            }

            @Override
            public void debug(String message) {
                System.out.println("DEBUG: " + message);
            }
        });
        client.setHost("192.168.1.120", 80);
        client.addBasicAuth("admin", "8uj1HyAD4tqNNtkGkFe");
        master = new EP8E001(client);
        sensor = new P3PC201(master, "master1port1");
    }

    @Test
    public void testDump() {
        sensor.dumpToConsole();
    }

    @Test
    public void testVendorName() throws Exception {
        String vendorName = sensor.getVendorName();
        System.out.println("Vendor Name: " + vendorName);
    }
}
