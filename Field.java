
public class Field {
	
    public static enum Types {
        FLOOR,
        WALL,
        UNKOWN
    }

    private Field.Types type;
    private Position position;
    private Gem gem;

    public Field(int x, int y, Field.Types type) {
        position = new Position(x, y);
        this.type = type;
    }

/*     public void addNeighbour(Field neighbour) {
        if (neighbour != null && !neighbours.contains(neighbour)) {
            neighbours.add(neighbour);
        }
    }

    public List<Field> getNeighbours() {
        return Collections.unmodifiableList(neighbours);
    } */

    public Position getPosition() {
        return position;
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }

    public Field.Types getType() {
        return type;
    }

    public void setType(Field.Types type) {
        this.type = type;
    }

    public boolean isAccessible() {
        return this.getType()==Field.Types.FLOOR ? true : false;
    }

    public int getDistance(Field field) {
        return Math.abs(field.getPosition().getX() - position.getX()) + Math.abs(field.getPosition().getY() - position.getY());
    }

    public void setGem(Gem gem) {
        this.gem = gem;
    }

    public Gem getGem() {
        return gem;
    }

    public void removeGem() {
        gem = null;
    }

    @Override
    public String toString() {
        return "Field{" +
                "position=" + position +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Field other = (Field) obj;
        return this.getX() == other.getX() && this.getY() == other.getY();
    }

    @Override
    public int hashCode() {
        return 31 * getX() + getY();
    }

}