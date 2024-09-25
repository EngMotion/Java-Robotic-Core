import com.nanotec.nanolib.*;
import com.nanotec.nanolib.helper.NanolibHelper;

public class Nanotec {
    public static void main(String[] args) {
        String port = "COM3";
        try {
            NanolibHelper nanolibHelper = new NanolibHelper();
            nanolibHelper.setup();
            BusHWIdVector busHwIds = nanolibHelper.getBusHardware();
            if (busHwIds.isEmpty()) {
                System.out.println("No bus hardware found");
                return;
            }

            int lineNum = 0;
            BusHardwareId busHwId = null;

            // just for better overview: print out available hardware
            for (BusHardwareId adapter : busHwIds) {
                if (adapter.getName().contains(port)) {
                    busHwId = adapter;
                }
            }
            if (busHwId == null) {
                System.out.println("No bus hardware found");
                return;
            }

            BusHardwareOptions busHwOptions = nanolibHelper.createBusHardwareOptions(busHwId);
            nanolibHelper.openBusHardware(busHwId, busHwOptions);
            DeviceId deviceId = new DeviceId(busHwId,1 , "Carosello");
            DeviceHandle deviceHandle = nanolibHelper.createDevice(deviceId);
            nanolibHelper.connectDevice(deviceHandle);
            nanolibHelper.writeNumber(deviceHandle, 3, new OdIndex(0x6060, (short) 0x0),8);
            nanolibHelper.writeNumber(deviceHandle, -50, new OdIndex(0x60FF, (short) 0x0),32);
            nanolibHelper.writeNumber(deviceHandle, 6, new OdIndex(0x6040, (short) 0x00), 16);
            while (true){
                long statusWord = nanolibHelper.readNumber(deviceHandle, new OdIndex(0x6041, (short) 0x00));
                System.out.println("1 - "+statusWord);
                if ((statusWord & 0xEF)==0x21) break;
                Thread.sleep(100);
            }
            nanolibHelper.writeNumber(deviceHandle, 7, new OdIndex(0x6040, (short) 0x00), 16);
            while (true){
                long statusWord = nanolibHelper.readNumber(deviceHandle, new OdIndex(0x6041, (short) 0x00));
                System.out.println("2 - "+statusWord);
                if ((statusWord & 0xEF)==0x23) break;
                Thread.sleep(100);
            }
            nanolibHelper.writeNumber(deviceHandle, 15, new OdIndex(0x6040, (short) 0x00), 16);
            while (true){
                long statusWord = nanolibHelper.readNumber(deviceHandle, new OdIndex(0x6041, (short) 0x00));
                System.out.println("3 - "+statusWord);
                if ((statusWord & 0xEF)==0x27) break;
                Thread.sleep(100);
            }
            for (int i =0;i<10;i++){
                Thread.sleep(1000);
                nanolibHelper.writeNumber(deviceHandle, 50, new OdIndex(0x60FF, (short) 0x0),32);
                Thread.sleep(1000);
                nanolibHelper.writeNumber(deviceHandle, -50, new OdIndex(0x60FF, (short) 0x0),32);
            }
            nanolibHelper.writeNumber(deviceHandle, 0, new OdIndex(0x60FF, (short) 0x0),32);
            nanolibHelper.writeNumber(deviceHandle, 0, new OdIndex(0x6040, (short) 0x00), 16);
        } catch (NanolibHelper.NanolibException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
