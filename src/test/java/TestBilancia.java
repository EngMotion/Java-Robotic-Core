import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.StatusMode;
import com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.iDM_RS;
import com.lucaf.robotic_core.State;
import com.kern.scale.SerialCommunication;

import java.util.HashMap;

public class TestBilancia {
    public static void main(String[] args) {

        try {
            SerialParameters params = new SerialParameters();
            params.setPortName("COM5");
            params.setBaudRate(115200);
            params.setDatabits(8);
            params.setParity("None");
            params.setStopbits(1);
            params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
            ModbusSerialMaster master = new ModbusSerialMaster(
                    params
            );
            master.connect();
            System.out.println("Connected");
            iDM_RS iDM_rs = new iDM_RS(master, (byte) 0x01, new HashMap<>(), new State() {
                @Override
                public void notifyStateChange() {

                }
            });
            iDM_rs.stop();
            iDM_rs.setPositioningMode();
            iDM_rs.setRelativePositioning(true);
            iDM_rs.setDeceleration(100);
            iDM_rs.setAcceleration(100);
            iDM_rs.setSpeed(400);
            long now = System.currentTimeMillis();
            iDM_rs.moveToPositionAndWait(-100000).get();
            System.out.println("Ci ho messo " + (System.currentTimeMillis()-now) + " ms");
            Thread.sleep(1000);

            System.out.println("Routine done");

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }
}
