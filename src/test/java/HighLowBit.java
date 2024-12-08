public class HighLowBit {
    public static void main(String[] args) {
        int n = 70000;
        int high = n >> 16;
        int low = n & 0xFFFF;
        System.out.println("High: " + high);
        System.out.println("Low: " + low);
    }
}
