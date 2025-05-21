import com.lucaf.robotic_core.Logger;
import com.lucaf.robotic_core.MOXMEC.Serial;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import jssc.SerialPortException;

public class RG024A {
    public static void main(String[] args) throws SerialPortException, DeviceCommunicationException {
        System.out.println("RG024A Motor Controller");
        Logger logger = new Logger() {
            @Override
            public void log(String message) {
                System.out.println(message);
            }

            @Override
            public void error(String message) {
                System.out.println(message);
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
        Serial serial = new Serial("COM8");
        serial.setLogger(logger);
        com.lucaf.robotic_core.MOXMEC.RG024A.RG024A rg024A = new com.lucaf.robotic_core.MOXMEC.RG024A.RG024A(serial, logger);
        rg024A.setFrequency(2000);
        rg024A.setAmplitude(50);
        rg024A.save();
        int ramp = rg024A.getRamp();
        int amplitude = rg024A.getAmplitude();
        int frequency = rg024A.getFrequency();
        System.out.println("Ramp: " + ramp);
        System.out.println("Amplitude: " + amplitude);
        System.out.println("Frequency: " + frequency);
    }
}
