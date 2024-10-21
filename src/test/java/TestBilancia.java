import com.lucaf.robotic_core.KERN.PCB_3;


public class TestBilancia {
    public static void main(String[] args) {
        try {
            PCB_3 pcb_3 = new PCB_3("COM8");
            System.out.println(pcb_3.getReading());
            pcb_3.tare().get();
            System.exit(0);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }
}
