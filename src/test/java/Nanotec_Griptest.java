import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.NANOTEC.PD4E_RTU.Constants;
import com.lucaf.robotic_core.NANOTEC.PD4E_RTU.PD4E;
import com.lucaf.robotic_core.State;
import com.nanotec.nanolib.*;
import com.nanotec.nanolib.helper.NanolibHelper;

import java.util.HashMap;

public class Nanotec_Griptest {
    public static void main(String[] args) {
        Logger logger = new Logger() {
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
                System.out.println(message);
            }

            @Override
            public void debug(String message) {
                System.out.println(message);
            }
        };
        String port = "COM3";
        try {
            NanolibHelper nanolibHelper = new NanolibHelper();
            nanolibHelper.setup();
            BusHWIdVector busHwIds = nanolibHelper.getBusHardware();
            if (busHwIds.isEmpty()) {
                System.out.println("No bus hardware found");
                return;
            }

            int lineNum = 0;
            BusHardwareId busHwId = null;

            // just for better overview: print out available hardware
            for (BusHardwareId adapter : busHwIds) {
                if (adapter.getName().contains(port)) {
                    busHwId = adapter;
                }
            }
            if (busHwId == null) {
                System.out.println("No bus hardware found");
                return;
            }

            BusHardwareOptions busHwOptions = nanolibHelper.createBusHardwareOptions(busHwId);
            nanolibHelper.openBusHardware(busHwId, busHwOptions);
            DeviceId deviceId = new DeviceId(busHwId, 1, "Carosello");
            DeviceHandle deviceHandle = nanolibHelper.createDevice(deviceId);
            nanolibHelper.connectDevice(deviceHandle);
            PD4E pd4e = new PD4E(nanolibHelper, deviceHandle, new HashMap<>(), new State() {
                @Override
                public void notifyStateChange() {

                }

                @Override
                public void notifyError() {

                }
            }, logger);
            pd4e.start(Constants.OperationMode.PROFILE_VELOCITY);


            for (int i = 0; i < 10; i++) {
                //Thread.sleep(5000);
                //pd4e.setVelocity(50);
                //Thread.sleep(1000);
                //pd4e.setVelocity(-50);
            }
            System.out.println(pd4e.getMaxCurrent()); //Default 2100
            pd4e.setMaxCurrent(3800);

            pd4e.setVelocity(-50);

            Thread.sleep(20000);

            pd4e.stop();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
