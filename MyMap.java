import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

public class MyMap {
    
    public static enum Directions {
        NORTH,
        EAST,
        SOUTH,
        WEST
    }

    private Map<Position, Field> fields = new HashMap<>();
    private int width;
    private int height;
    private boolean discovered = false;
    private Queue<Field> addedFields = new ArrayDeque<Field>();

    public MyMap(int width, int height) {
        this.width = width;
        this.height = height;

        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                Field field = new Field(x, y, Field.Types.UNKOWN);
                fields.put(field.getPosition(), field);
            }
        }
    }

        /**
     * Fügt ein Field in die Map ein.
     * Falls an der Position bereits ein Field existiert, wird es überschrieben.
     */
    public void addField(Field field) {
        if (field == null) return;
        fields.put(field.getPosition(), field);
        addedFields.add(field);
        //Utils.log("add field: [" + field.getPosition().getX() + ";  " + field.getPosition().getY() + "]\n");
    }

    /**
     * Liefert das Field an der angegebenen Position zurück.
     * @param pos Position des Feldes
     * @return Field an der Position oder null, falls keines existiert
     */
    public Field getField(Position pos) {
        if (pos == null) return null;
        return fields.get(pos);
    }

    public Field getField(int x, int y) {
        Field field = fields.get(new Position(x, y));

        if(field==null) {
            System.out.println("no field at [" + x + "; " + y + "] -> return null");
        }
        return field;
    }

    public Field getFloorNeighbours(Field field, MyMap.Directions direction) {
        if(field==null || direction==null) {
            throw new IllegalArgumentException("Field getFloorNeighbours(Field field, MyMap.Directions direction): arguments may not be null");
        }
        Field neighbour = getNeighbour(field, direction);
        if (neighbour == null) {
            return null;
        }
        return neighbour.getType() == Field.Types.FLOOR ? neighbour : null;
    }

    public Field getNeighbour(Position position, MyMap.Directions direction) {
        return getNeighbour(position.getX(), position.getY(), direction);
    }

    private Field getNeighbour(Field field, MyMap.Directions direction) {
        return getNeighbour(field.getX(), field.getY(), direction);
    }

    private Field getNeighbour(int x, int y, MyMap.Directions direction) {
        if(isInsideMap(x, y)) {
            if(direction==MyMap.Directions.NORTH) {
                y--;
            }
            else if(direction==MyMap.Directions.EAST) {
                x++;
            }
            else if(direction==MyMap.Directions.SOUTH) {
                y++;
            }
            else if(direction==MyMap.Directions.WEST) {
                x--;
            }

            if(x<0 || x>=width) {
                return null;
            }
            else if(y<0 || y>=height) {
                return null;
            }
            else {
                return getField(x, y);
            }
        }
        else {
            return null;
        }
    }

    public List<Field> getFloorFieldsWithUnkownNeighbours(Position positionBot) {
        List<Field> floorFieldsWithUnkownNeighbours = new LinkedList<>();

        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {  
                if(!(x==positionBot.getX() && y==positionBot.getY())) {
                    Field field = getField(x, y);
                    for(MyMap.Directions direction: MyMap.Directions.values()) {
                        Field neighbour = getNeighbour(field, direction);
                        if(field.getType()==Field.Types.FLOOR && neighbour.getType()==Field.Types.UNKOWN ) {
                            floorFieldsWithUnkownNeighbours.add(field);
                            break;
                        }
                    }
                }
            }
        }

        return floorFieldsWithUnkownNeighbours;
    }

    public Field getFloorFieldWithUnkownNeighbours(Position positionBot) {
        if(!discovered) {
            int x = positionBot.getX() - 1;
            int y = positionBot.getY() + 1;
            int steps = 4;
            int maxRounds;
            
            if(positionBot.getX()>width/2) {
                maxRounds = positionBot.getX()-2;
            }
            else {
                maxRounds = width-positionBot.getX()-2;
            }
            
            for(int rounds=1; rounds<=maxRounds; rounds++) {
                x--;
                y++;

                // right
                for(int i=1; i<=steps; i++) {
                    x++;
                    if(isInsideMap(x, y)) {
                        Field field = getField(x, y);
                        //Utils.log("[" + x + ", " + y + "]");
                        if(hasUnkownNeighbours(field)) {
                            return field;
                        }
                    }
                }
                //Utils.log("\n");

                // up
                for(int i=1; i<=steps; i++) {
                    y--;
                    if(isInsideMap(x, y)) {
                        Field field = getField(x, y);
                        //Utils.log("[" + x + ", " + y + "]");
                        if(hasUnkownNeighbours(field)) {
                            return field;
                        }
                    }
                }
                //Utils.log("\n");

                // left
                for(int i=1; i<=steps; i++) {
                    x--;
                    if(isInsideMap(x, y)) {
                        Field field = getField(x, y);
                        //Utils.log("[" + x + ", " + y + "]");
                        if(hasUnkownNeighbours(field)) {
                            return field;
                        }
                    }
                }
                //Utils.log("\n");

                // down
                for(int i=1; i<=steps; i++) {
                    y++;
                    if(isInsideMap(x, y)) {
                        Field field = getField(x, y);
                        //Utils.log("[" + x + ", " + y + "]");
                        if(hasUnkownNeighbours(field)) {
                            return field;
                        }
                    }
                }
                //Utils.log("\n");
                steps = steps + 2;
            }
        }

        discovered=true;
        Utils.log("map completely discovered!");
        return null;
    }

    public String createMap(Position botPosition) {
        StringBuffer sb = new StringBuffer();
        
        sb.append("arena:\n ");
        for(int x=0; x<width; x++) {
            String str = Integer.toString(x);
            sb.append(str.charAt(str.length()-1));
        }
         sb.append("\n");
        for(int y=0; y<height; y++) {
            String str = Integer.toString(y);
            char c = str.charAt(str.length()-1);
            sb.append(c);
            for(int x=0; x<width; x++) {
                if(x==botPosition.getX() && y==botPosition.getY()) {
                    sb.append("B");
                }
                else {
                    Field field = getField(x, y);
                    if (field.getType()==Field.Types.WALL) {
                        sb.append("W");
                    } 
                    else if (field.getType()==Field.Types.FLOOR) {
                        if (field.getGem()!=null) {
                            sb.append("G"); 
                        }
                        else {
                            sb.append(" "); 
                        }
                    }
                    else if (field.getType()==Field.Types.UNKOWN) {
                        sb.append("?");
                    }
                }
            }
            sb.append(c + "\n");
        }
         sb.append(" ");
        for(int x=0; x<width; x++) {
            String str = Integer.toString(x);
            sb.append(str.charAt(str.length()-1));
        }

        return sb.toString();
    }


    public Field getRandomFloorField(Random rng) {
        while(true) {
          int x = rng.nextInt(getWidth());
          int y = rng.nextInt(getHeight());
          Field field = getField(x, y);
          if(field.getType()==Field.Types.FLOOR) {
            return field;
          }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Prüfen, ob ein Field an der Position existiert
     */
    public boolean hasField(Position pos) {
        return pos != null && fields.containsKey(pos);
    }

    private boolean hasUnkownNeighbours(Field field) {
        if(field!=null) {
            for(MyMap.Directions direction: MyMap.Directions.values()) {
                Field neighbour = getNeighbour(field, direction);
                if(field.getType()==Field.Types.FLOOR && neighbour.getType()==Field.Types.UNKOWN ) {
                    return true;
                }
            }
        }

        return false;
    }    

    private boolean isInsideMap(int x, int y) {
        if(x>=0 && x<width) {
             if(y>=0 && y<height) {
                return true;
             }
        }

        return false;
    }
}
