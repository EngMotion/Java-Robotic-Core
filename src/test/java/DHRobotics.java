import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.lucaf.robotic_core.DH_ROBOTICS.RGI100_22.RGI100_22;
import com.lucaf.robotic_core.DH_ROBOTICS.SAC_N_M2.SAC_N;
import com.lucaf.robotic_core.State;

import java.util.HashMap;
import java.util.concurrent.Future;

public class DHRobotics {

    public static void main(String[] args){
        State state = new State() {
            @Override
            public void notifyStateChange() {

            }
        };
        try {
            SerialParameters params = new SerialParameters();
            params.setPortName("COM7");
            params.setBaudRate(115200);
            params.setDatabits(8);
            params.setParity("None");
            params.setStopbits(1);
            params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
            ModbusSerialMaster master = new ModbusSerialMaster(
                    params
            );
            master.connect();
            RGI100_22 rgi10022 = new RGI100_22(master, new HashMap<>(), state);
            rgi10022.setId((byte) 0x04);
            rgi10022.initialize();
            rgi10022.setGripForce(100);
            rgi10022.setRotationForce(20);
            rgi10022.setRotationSpeed(20);
            SAC_N sac_n = new SAC_N(master, new HashMap<>(), state);
            sac_n.setId((byte) 0x01);
            sac_n.initialize();
            sac_n.setSpeed(100);
            sac_n.setAcceleration(100);
            while (true){
                Future<Boolean> task5 = sac_n.move_absoluteAndWait(7500);
                Future<Boolean> task1 = rgi10022.moveToAbsoluteAngleAndWait(200);
                Future<Boolean> task2 = rgi10022.setGripPositionAndWait(10);
                task1.get();
                task2.get();
                task5.get();

                Future<Boolean> task6 = sac_n.move_absoluteAndWait(500);
                Future<Boolean> task3 = rgi10022.moveToAbsoluteAngleAndWait(-200);
                Future<Boolean> task4 = rgi10022.setGripPositionAndWait(1000);
                task3.get();
                task4.get();
                task6.get();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
