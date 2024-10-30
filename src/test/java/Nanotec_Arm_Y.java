import com.lucaf.robotic_core.NANOTEC.PD4E_RTU.Constants;
import com.lucaf.robotic_core.NANOTEC.PD4E_RTU.PD4E;
import com.lucaf.robotic_core.NANOTEC.PD4E_RTU.UnitControl;
import com.lucaf.robotic_core.Pair;
import com.lucaf.robotic_core.State;
import com.nanotec.nanolib.*;
import com.nanotec.nanolib.helper.NanolibHelper;

import java.util.HashMap;

public class Nanotec_Arm_Y {
    public static void main(String[] args) {
        String port = "COM10";
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
            DeviceId deviceId = new DeviceId(busHwId, 1, "ArmY");
            DeviceHandle deviceHandle = nanolibHelper.createDevice(deviceId);
            nanolibHelper.connectDevice(deviceHandle);
            PD4E pd4e = new PD4E(nanolibHelper, deviceHandle, new HashMap<>(), new State() {
                @Override
                public void notifyStateChange() {

                }

                @Override
                public void notifyError() {

                }
            });
           //pd4e.setBrakeAddress(1);
            //pd4e.stop();
            pd4e.setMaxSpeed(200);
            pd4e.setAcceleration(100);
            pd4e.setDeceleration(100);
            //pd4e.home(35);
            pd4e.start(Constants.OperationMode.PROFILE_POSITION);
            pd4e.setUnitPositionFactor(-1);
            pd4e.setUnitPositionUnit(UnitControl.Units.ENCODER);
            pd4e.setMaxCurrent(4000);
            pd4e.writeRegister( new Pair<>(new OdIndex(0x3210, (short) 0x0B),32),1000);
            System.out.println(pd4e.getUnitPosition().toString());
            //pd4e.setPositionRelative(-4000).get();
            //pd4e.setPositionRelative(20).get();
            while (true){
                //pd4e.setPositionRelative(20000).get();
                //pd4e.setPositionRelative(-2000).get();
            }
            //pd4e.setPositionAbsolute(0).get();
            //One turn 13228
            //pd4e.stop();

            //pd4e.setBrakeStatus(true);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
