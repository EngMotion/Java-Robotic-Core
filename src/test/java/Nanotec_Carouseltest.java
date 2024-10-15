import com.lucaf.robotic_core.NANOTEC.PD4E_RTU.Constants;
import com.lucaf.robotic_core.NANOTEC.PD4E_RTU.PD4E;
import com.lucaf.robotic_core.State;
import com.nanotec.nanolib.*;
import com.nanotec.nanolib.helper.NanolibHelper;

import java.util.HashMap;

public class Nanotec_Carouseltest {
    public static void main(String[] args) {
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
            });
            pd4e.setBrakeAddress(1);
            pd4e.start(Constants.OperationMode.PROFILE_POSITION,21);


            /*for (int i = 0; i < 10; i++) {
                //Thread.sleep(5000);
                //pd4e.setVelocity(50);
                //Thread.sleep(1000);
                //pd4e.setVelocity(-50);
            }
            System.out.println(pd4e.getAcceleration()); //Default 500
            */
            pd4e.setAcceleration(200);
            pd4e.setPositionAbsolute(0);
            for (int i = 0; i < 10; i++) {
                Thread.sleep(1000);
                pd4e.setPositionRelative(13200/12);
            }

            Thread.sleep(2000);

            //One turn 13228
            pd4e.stop();

            pd4e.setBrakeStatus(true);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
