public abstract class Tactic {

    private MyMap map;

    public Tactic(MyMap map) {
        this.map = map;
    }

    public MyMap getMyMap() {
        return map;
    }

    abstract MyMap.Directions determineDirection(Position currentPosition, Position target);
    abstract void reset();
}