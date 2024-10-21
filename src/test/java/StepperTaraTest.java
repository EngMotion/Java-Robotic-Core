import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.lucaf.robotic_core.KERN.PCB_3;
import com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.iDM_RS;
import com.lucaf.robotic_core.State;

import java.util.HashMap;

public class StepperTaraTest {
    public static void main(String[] args) {
        try {
            PCB_3 bilancia = new PCB_3("COM8");
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

            int cicli = 10;
            int quantita = 10000;

            bilancia.tare().get();

            for (int i = 0; i < cicli; i++) {
                iDM_rs.moveToPositionAndWait(quantita).get();
                System.out.println(bilancia.getReading());
                bilancia.tare().get();
            }
            System.exit(0);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
