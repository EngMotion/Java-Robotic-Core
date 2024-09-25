import com.lucaf.robotic_core.NANOTEC.PD4E_RTU.ControlWord;

public class Nanotec_ControlWord {
    public static void main(String[] args){
        ControlWord controlWord = new ControlWord(6);
        System.out.println(controlWord);
        ControlWord controlWord1 = new ControlWord(7);
        System.out.println(controlWord1);
        ControlWord controlWord2 = new ControlWord(15);
        System.out.println(controlWord2);

        ControlWord controlWord3 = new ControlWord();
        controlWord3.setSwitchOn(true);
        controlWord3.setEnableVoltage(true);
        controlWord3.setQuickStop(true);

        System.out.println(controlWord3);
        System.out.println(controlWord3.toInt());
    }
}
