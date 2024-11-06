import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.lucaf.robotic_core.KERN.PCB_3;
import com.lucaf.robotic_core.KERN.PLJ_1200;
import com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.iDM_RS;
import com.lucaf.robotic_core.State;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class StepperTaraTest {
    public static void main(String[] args) {
        try {
            //PCB_3 bilancia = new PCB_3("COM6");
            PLJ_1200 bilancia = new PLJ_1200("COM6");
            SerialParameters params = new SerialParameters();
            params.setPortName("COM5");
            params.setBaudRate(115200);
            //params.setBaudRate(57600);
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
            //iDM_rs.setVelocityMode();
            iDM_rs.setRelativePositioning(true);
            iDM_rs.setDeceleration(100);
            iDM_rs.setAcceleration(100);
            iDM_rs.setSpeed(150);

            double dosaggio = 10;
            int cicli = 20;
            //int quantita = (int) Math.round((dosaggio / 0.) * 1667);
            int quantita = 3333;
            int back = 0;
            String scale = "on";

            System.out.println("Dosaggio: " + dosaggio + " - Passi: " + quantita);

            List<Double> numeri = new ArrayList<>();
            List<Double> tempi = new ArrayList<>();

            if (scale == "on") {
                bilancia.tare().get();
            }

            for (int i = 0; i < cicli; i++) {
                double now = System.currentTimeMillis();
                iDM_rs.moveToPositionAndWait(-quantita).get();
                //System.out.println("Ci ho messo " + (System.currentTimeMillis()-now) + " ms");
                tempi.add(System.currentTimeMillis()-now);
                iDM_rs.moveToPositionAndWait(back).get();

                if (scale == "on") {
                    Thread.sleep(8000);
                    System.out.println(String.valueOf(bilancia.getReading()).replace(".",","));
                    numeri.add(bilancia.getReading());
                    Thread.sleep(200);
                } else {
                    Thread.sleep(2000);
                }

                if (scale == "on") {
                    bilancia.tare().get();
                }
            }

           bilancia.closePort();

            double media = numeri.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0); // Ritorna 0.0 se la lista è vuota

            double tempo = tempi.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0); // Ritorna 0.0 se la lista è vuota

            String tempo_s = String.format("%.2f", tempo);
            String media_s = String.format("%.2f", media);

            // Stampa il risultato
            System.out.println("Media pesate: " + media_s + " g");
            System.out.println("Tempo pesate: " + tempo_s + " ms");

            System.exit(0);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
