import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.lucaf.robotic_core.KERN.PCB_3;
import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.STEPPERONLINE.iDM_RS.IDM_RS;
import com.lucaf.robotic_core.State;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StepperTaraTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

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
            PCB_3 bilancia = new PCB_3("COM6");
            //PLJ_1200 bilancia = new PLJ_1200("COM6");
            //PLJ_1200 bilancia = null;
            SerialParameters params = new SerialParameters();
            params.setPortName("COM5");
            //params.setBaudRate(115200); //stepper
            params.setBaudRate(57600); //servo
            params.setDatabits(8);
            params.setParity("None");
            params.setStopbits(1);
            params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
            ModbusSerialMaster master = new ModbusSerialMaster(
                    params
            );
            master.connect();
            IDM_RS iDM_rs = new IDM_RS(master, (byte) 0x01, new HashMap<>(), new State() {
                @Override
                public void notifyStateChange() {

                }

                @Override
                public void notifyError() {

                }
            }, logger);
            iDM_rs.stop();
            iDM_rs.setPositioningMode();
            //iDM_rs.setVelocityMode();
            iDM_rs.setRelativePositioning(true);
            iDM_rs.setDeceleration(100);
            iDM_rs.setAcceleration(100);
            iDM_rs.setSpeed(1200);

            double dosaggio = 10;

            //int quantita = (int) Math.round((dosaggio / 0.) * 1667);


            int cicli = 200;
            int quantita = 356238;



            int back = 5000;
            String scale = "on0";

            System.out.println("Dosaggio: " + dosaggio + " - Impulsi: " + quantita);

            List<Double> numeri = new ArrayList<>();
            List<Double> tempi = new ArrayList<>();

            if (scale == "on") {
                if (bilancia != null) bilancia.tare().get();
            }

            for (int i = 0; i < cicli; i++) {
                double now = System.currentTimeMillis();
                iDM_rs.moveToPositionAndWait(-quantita).get();
                //System.out.println("Ci ho messo " + (System.currentTimeMillis()-now) + " ms");
                tempi.add(System.currentTimeMillis() - now);
                iDM_rs.moveToPositionAndWait(back).get();

                if (scale == "on") {
                    Thread.sleep(12000);
                    System.out.println(String.valueOf(bilancia.getReading()).replace(".", ","));
                    numeri.add(bilancia.getReading());
                    Thread.sleep(200);
                } else {
                    System.out.println("Premere Invio per continuare...");
                    scanner.nextLine(); // Attende che l'utente prema Invio
                    //Thread.sleep(12000);
                }

                if (scale == "on") {
                    if (bilancia != null) bilancia.tare().get();
                }
            }

            if (bilancia != null) bilancia.closePort();

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
