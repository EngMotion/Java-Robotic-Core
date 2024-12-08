import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.StatusMode;
import com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.iDM_RS;
import com.lucaf.robotic_core.State;

import java.util.HashMap;

public class StepperOnline {
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
        try {
            SerialParameters params = new SerialParameters();
            params.setPortName("COM5");
            params.setBaudRate(57600);
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

                @Override
                public void notifyError() {

                }
            }, logger);
            iDM_rs.stop();
            iDM_rs.setVelocityMode();
            //iDM_rs.setRelativePositioning(true);
            iDM_rs.setDeceleration(50);
            iDM_rs.setAcceleration(50);
            iDM_rs.setSpeed(600);
            Thread.sleep(5000);
            iDM_rs.setSpeed(0);
            long now = System.currentTimeMillis();
            //iDM_rs.moveToPositionAndWait(10000).get();
            System.out.println("Ci ho messo " + (System.currentTimeMillis()-now) + " ms");
            Thread.sleep(1000);

            System.out.println("Routine done");

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }
}
