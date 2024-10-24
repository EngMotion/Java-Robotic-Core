import com.lucaf.robotic_core.NANOTEC.PD4E_RTU.PD4E;
import com.lucaf.robotic_core.State;
import com.nanotec.nanolib.*;
import com.nanotec.nanolib.helper.NanolibHelper;

import java.util.HashMap;

public class NanotecCan {
    public static void main(String[] args){
        try{
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
                System.out.println(adapter.toString());
                if (adapter.getName().contains("USB Bus")) {
                    busHwId = adapter;
                    System.out.println("Found BUS");
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
            System.out.println(deviceHandle.toString());
            nanolibHelper.connectDevice(deviceHandle);
            PD4E pd4e = new PD4E(nanolibHelper, deviceHandle, new HashMap<>(), new State() {
                @Override
                public void notifyStateChange() {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
