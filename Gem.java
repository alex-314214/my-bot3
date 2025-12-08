public class Gem {

    private Field field;
    private int ttl; // Time-To-Live

    public Gem(Field field, int ttl) {
        this.field = field;
        this.ttl = ttl;
        this.field.setGem(this);
    }

    public Field getField() {
        return field;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public Gem reduceTtl() {
        ttl--;
        if(ttl==0) {
            field.removeGem();
        }
        return field.getGem();
    }

    @Override
    public String toString() {
        return "Gem{" +
                "x=" + field.getPosition().getX() +
                ", y=" + field.getPosition().getY() +
                ", ttl=" + ttl +
                '}';
    }
}
