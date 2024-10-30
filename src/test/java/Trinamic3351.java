import com.lucaf.robotic_core.State;
import com.lucaf.robotic_core.TRINAMIC.TMCM_3351.TMCM_3351;
import com.lucaf.robotic_core.TRINAMIC.TMCM_3351.TMCM_3351_MOTOR;
import com.lucaf.robotic_core.TRINAMIC.USB;
import com.lucaf.robotic_core.exception.ConfigurationException;
import com.lucaf.robotic_core.exception.DeviceCommunicationException;
import jssc.SerialPortException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.lucaf.robotic_core.TRINAMIC.TMCM_3351.Constants.*;
public class Trinamic3351 {
    public static void main(String[] args) {
        try {
            USB usb = new USB("COM7");
            State state = new State() {
                @Override
                public void notifyStateChange() {

                }

                @Override
                public void notifyError() {

                }
            };
            TMCM_3351 tmcm_3351 = new TMCM_3351(usb, new HashMap<>(), state);
            TMCM_3351_MOTOR motor0 = tmcm_3351.getMotor((byte) 0x00, new HashMap<>());
            motor0.setParameters(
                    Map.ofEntries(
                            Map.entry("PARAM_MAX_CURRENT", 100),
                            Map.entry("PARAM_START_VELOCITY", 0),
                            Map.entry("PARAM_MICROSTEP_RESOLUTION", 8),
                            Map.entry("PARAM_REFERENCE_SEARCH_MODE", 8),
                            Map.entry("PARAM_REFERENCE_SWITCH_SPEED", 3000),
                            Map.entry("PARAM_REFERENCE_SEARCH_SPEED", 36000),
                            Map.entry("PARAM_POWER_DOWN_DELAY", 5),
                            Map.entry("PARAM_STOP_ON_STALL", 0),
                            Map.entry("PARAM_ENCODER_RESOLUTION", 40000),
                            Map.entry("PARAM_ENCODER_MAX_DEVIATION", 0),
                            Map.entry("PARAM_ACTUAL_POSITION", 0),
                            Map.entry("PARAM_CL_MODE",1),
                            Map.entry("PARAM_RELATIVE_POSITIONING_OPTION",2)
                    ));

            TMCM_3351_MOTOR motor1 = tmcm_3351.getMotor((byte) 0x01, new HashMap<>());
            motor1.setParameters(
                    Map.of("PARAM_MAX_CURRENT", 100)
            );
            TMCM_3351_MOTOR motor2 = tmcm_3351.getMotor((byte) 0x02, new HashMap<>());
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
            motor0.moveToRelativePositionAndWait(2).get();
            System.out.println(motor0.getParameter(PARAM_ENCODER_POSITION));
        } catch (SerialPortException e) {
            throw new RuntimeException(e);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        } catch (DeviceCommunicationException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
