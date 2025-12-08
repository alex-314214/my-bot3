public class OutOfBorder extends Exception {

    public OutOfBorder() {
        super();
    }

    public OutOfBorder(String msg) {
        super(msg);
    }

    public OutOfBorder(int x, int y) {
        super("The position [" + x +", " + y + "] is outside the allowed arena area.!");
    }
    
}