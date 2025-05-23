import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.DigitalInputs;
import com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.DigitalOutput;
import com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.DigitalOutputs;
import com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.IDM_RS;
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
                //System.out.println(message);
            }
        };
        try {
            SerialParameters params = new SerialParameters();
            params.setPortName("COM8");
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
            IDM_RS iDM_rs = new IDM_RS(master, (byte) 0x05, new HashMap<>(), new State() {
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
            //iDM_rs.setSpeed(100);
            //Thread.sleep(1000);
            //iDM_rs.setSpeed(0);
            boolean status = true;
            while (true){
                status = !status;
                DigitalOutput digitalOutput = iDM_rs.getDigitalOutput(1);
                digitalOutput.setNormally_closed(status);
                iDM_rs.setDigitalOutput(1, digitalOutput);
                Thread.sleep(!status ? 10000 : 100);
                DigitalInputs inputs = iDM_rs.getDigitalInputs();
                System.out.println(inputs);
                if (false) break;
            }
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
