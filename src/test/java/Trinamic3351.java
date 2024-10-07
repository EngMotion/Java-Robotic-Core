import com.lucaf.robotic_core.TRINAMIC.TMCM_3351.TMCM_3351;
import com.lucaf.robotic_core.TRINAMIC.TMCM_3351.TMCM_3351_MOTOR;
import com.lucaf.robotic_core.TRINAMIC.USB;
import com.lucaf.robotic_core.exception.ConfigurationException;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import jssc.SerialPortException;

import java.util.Map;

public class Trinamic3351 {
    public static void main(String[] args) {
        try {
            USB usb = new USB("COM4");
            TMCM_3351 tmcm_3351 = new TMCM_3351(usb);
            TMCM_3351_MOTOR motor0 = tmcm_3351.getMotor((byte) 0x00);
            motor0.setParameters(
                    Map.of("PARAM_MAX_CURRENT", 100)
            );
            TMCM_3351_MOTOR motor1 = tmcm_3351.getMotor((byte) 0x01);
            motor1.setParameters(
                    Map.of("PARAM_MAX_CURRENT", 100)
            );
            TMCM_3351_MOTOR motor2 = tmcm_3351.getMotor((byte) 0x02);
            motor2.setParameters(
                    Map.of("PARAM_MAX_CURRENT", 100)
            );
            motor0.rotateRight(100000);
            motor1.rotateRight(100000);
            motor2.rotateRight(100000);
            Thread.sleep(5000);
            motor0.rotateRight(0);
            motor1.rotateRight(0);
            motor2.rotateRight(0);

        } catch (SerialPortException e) {
            throw new RuntimeException(e);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        } catch (DeviceCommunicationException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
