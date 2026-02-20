package com.lucaf.robotic_core.wenglor;

import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.dataInterfaces.tcp.HttpJsonConnector;
import com.lucaf.robotic_core.wenglor.ep8e001.EP8E001;
import com.lucaf.robotic_core.wenglor.ep8e001.config.WenglorEP8E001Config;
import com.lucaf.robotic_core.wenglor.p3pc201.P3PC201;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WenglorTest {
    public static EP8E001 create(WenglorEP8E001Config config){
        HttpJsonConnector client = new HttpJsonConnector("Wenglor", new Logger() {
            @Override
            public void log(String message) {
                System.out.println(message);
            }

            @Override
            public void error(String message) {
                System.out.println(message);
            }

            @Override
            public void warn(String message) {
                System.out.println(message);
            }

            @Override
            public void debug(String message) {
                System.out.println(message);
            }
        });
        client.setHost("192.168.3.77", 8047);
        client.setMasterID(config.getMasterID());
        return new EP8E001(client);
    }


    @Test
    public void dumpTest() throws InterruptedException {
        WenglorEP8E001Config config = new WenglorEP8E001Config();
        config.setMasterID(1);
        EP8E001 master = create(config);
        P3PC201 x1Sensor = new P3PC201(master, "master1port1");
        x1Sensor.dumpToConsole();
    }

    @Test
    public void lightEmitTest() throws InterruptedException, IOException {
        WenglorEP8E001Config config = new WenglorEP8E001Config();
        config.setMasterID(1);
        EP8E001 master = create(config);
        P3PC201 x1Sensor = new P3PC201(master, "master1port1");
        x1Sensor.setLightEmitting(true);
        assertTrue(x1Sensor.isLightEmitting());
    }

    @Test
    public void lightEmitTest2() throws InterruptedException, IOException {
        WenglorEP8E001Config config = new WenglorEP8E001Config();
        config.setMasterID(1);
        EP8E001 master = create(config);
        P3PC201 x1Sensor = new P3PC201(master, "master1port1");
        x1Sensor.setLightEmitting(false);
        assertFalse(x1Sensor.isLightEmitting());
    }
}
