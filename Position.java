import java.util.Objects;

public class Position implements Cloneable {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // distance in steps
    public int getDistance(Position position) {
        // System.err.println("dist: " + (position.getX() - x) + Math.abs(position.getY() - y));
        return Math.abs(position.getX() - x) + Math.abs(position.getY() - y);
    }

    // Wichtig für HashMap
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position pos = (Position) o;
        return x == pos.x && y == pos.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
    }

    @Override
    public Position clone() {
        try {
            return (Position) super.clone();
        } 
        catch (CloneNotSupportedException e) {
            // Sollte nicht passieren, da Cloneable implementiert wird
            throw new AssertionError();
        }
    }
}
