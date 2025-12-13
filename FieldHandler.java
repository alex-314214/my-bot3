import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FieldHandler {

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

    public FieldHandler(int width, int height) {
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
        //log("add field: [" + field.getPosition().getX() + ";  " + field.getPosition().getY() + "]\n");
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

    /**
     * Prüfen, ob ein Field an der Position existiert
     */
    public boolean hasField(Position pos) {
        return pos != null && fields.containsKey(pos);
    }

    public String createMap(Position botPosition) {
        StringBuffer sb = new StringBuffer();
        
        sb.append("\narena:\n ");
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

    public Field getNeighbour(Position pos, FieldHandler.Directions dir) {
        return getNeighbour(getField(pos), dir);
    }

    public Field getNeighbour(Field field, FieldHandler.Directions dir) {
        if(field!=null) {
            int x = field.getX();
            int y = field.getY();

            if(dir==FieldHandler.Directions.NORTH) {
                y--;
            }
            else if(dir==FieldHandler.Directions.EAST) {
                x++;
            }
            else if(dir==FieldHandler.Directions.SOUTH) {
                y++;
            }
            else if(dir==FieldHandler.Directions.WEST) {
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

    public Field getFloorFieldWithUnkownNeighbours(Position positionBot) {
        if(!discovered) {
            int x = positionBot.getX();
            int y = positionBot.getY();
            int skip = 0;
            int max;

            if(positionBot.getX()>width/2) {
                max = positionBot.getX()-2;
            }
            else {
                max = width-positionBot.getX()-2;
            }

            for(int i=0; i<=max; i++) {
                if(skip==0) {
                    y=positionBot.getY()+2+i;
                    if(y>height-1) {
                        y=height-1;
                    }
                    x=positionBot.getX()-2-i;
                    if(x<0) {
                        x=0;
                    }
                }
                log("i: "  + i + "\t[" + x + "; " + y + "]");

                int xMin = Math.max(positionBot.getX() - 2 - i, 0);
                int xMax = Math.min(positionBot.getX() + 2 + i, width-1);
                int yMin = Math.max(positionBot.getY() - 2 - i, 0);
                int yMax = Math.min(positionBot.getY() + 2 + i, height-1);
                
                log("xMin: " + xMin);
                log("xMax:" + xMax);
                log("yMin:" + yMin);
                log("yMax: " + yMax);

                // right
                if(skip==0) {
                    while(x<Math.min(positionBot.getX() + 2 + i, width-1)) { //x<xMax) {
                        if(x<width-1) {
                            x++;
                            Field field = getField(x, y);
                            if(hasUnkownNeighbours(field)) {
                                return field;
                            }
                        }
                        else {
                            y = yMin;
                            //x++;
                            skip++;
                            if(yMin>positionBot.getY() - 2 - i) {
                                skip++;
                            }
                            log("set to: [" + x + ", " + y + "]  skip: " + skip);
                            break;
                        }
                    }
                    log("");
                }
                else {
                    skip--;
                }

                //  up
                if(skip==0) {
                    while(y>positionBot.getY() - 2 - i) { // y>yMin) {
                        if(y>0) {
                            y--;
                            Field field = getField(x, y);
                            if(hasUnkownNeighbours(field)) {
                                return field;
                            }
                        }
                        else {
                            x = xMin;
                            //y--;
                            skip++;
                            if(xMin>positionBot.getX() - 2 - i) {
                                skip++;
                            }
                            log("set to: [" + x + ", " + y + "]  skip: " + skip);
                            break;
                        }
                    }
                    log("");
                }
                else {
                    skip--;
                }

                // left
                if(skip==0) {
                    while(x>positionBot.getX() - 2 - i) { //xMin) {
                        if(x>0) {
                            x--;
                            Field field = getField(x, y);
                            if(hasUnkownNeighbours(field)) {
                                return field;
                            }
                        }
                        else {
                            y = yMax;
                            //x--;
                            skip++;
                            if(yMax<positionBot.getY() + 2 + i) {
                                skip++;
                            }
                            log("set to: [" + x + ", " + y + "]  skip: " + skip);
                            break;
                        }
                    }
                    log("");
                } 
                else {
                    skip--;
                }          

                // down
                if(skip==0) {
                    while(y<positionBot.getY() + 2 + i) { //yMax) {
                        if(y<height-1) {
                            y++;
                            Field field = getField(x, y);
                            if(hasUnkownNeighbours(field)) {
                                return field;
                            }
                        }
                        else {
                            x = xMax+1;
                            //y++;
                            skip++;
                            if(xMax<positionBot.getX() + 2 + i) {
                                skip++;
                            }
                            log("set to: [" + x + ", " + y + "]  skip: " + skip);
                            break;
                        }
                    }
                    log("");
                }
                else {
                    skip--;
                }
            }
        }
        discovered=true;
        return null;
    }

    private boolean hasUnkownNeighbours(Field field) {
        if(field!=null) {
            log("[" + field.getX() + ", " + field.getY() + "]");
        
            for(FieldHandler.Directions direction: FieldHandler.Directions.values()) {
                Field neighbour = getNeighbour(field, direction);
                if(field.getType()==Field.Types.FLOOR && neighbour.getType()==Field.Types.UNKOWN ) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<Field> getFloorFieldsWithUnkownNeighbours(Position positionBot) {
        List<Field> floorFieldsWithUnkownNeighbours = new LinkedList<>();

        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {  
                if(!(x==positionBot.getX() && y==positionBot.getY())) {
                    Field field = getField(x, y);
                    for(FieldHandler.Directions direction: FieldHandler.Directions.values()) {
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

    public List<Field> getFloorFieldsWithUnkownNeighbours(List<Field> fields) {
        List<Field> floorFieldsWithUnkownNeighbours = new LinkedList<>();

        for(Field field: fields) {
            //Bot.tfh.append("\npossible Field: [" + field.getX() + ", " + field.getY() + "]\n");

            for(FieldHandler.Directions direction: FieldHandler.Directions.values()) {
                Field neighbour = getNeighbour(field, direction);
                if(field.getType()==Field.Types.FLOOR && neighbour.getType()==Field.Types.UNKOWN ) {
                    floorFieldsWithUnkownNeighbours.add(field);
                    break;
                }
            }
        }

        return floorFieldsWithUnkownNeighbours;
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

    private void log(String text) { 
        if (Bot.ENABLE_LOGGING) {
            Bot.LOGGER.log(text);
        }
    }

}