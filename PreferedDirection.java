import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreferedDirection extends Tactic {

    private Position currentPosition;
    private Set<Field> doNotEnterFields = new HashSet<>();

    public PreferedDirection(MyMap map) {
        super(map);
    }

    private boolean checkDirection(MyMap.Directions direction) {
        Field neighbour = getMyMap().getNeighbour(currentPosition, direction);
        if(neighbour.isAccessible() && !doNotEnterFields.contains(neighbour)) {
            return true;
        }
        return false;
    }

    @Override
    public MyMap.Directions determineDirection(Position currentPosition, Position target) {
        List<MyMap.Directions> advantageousDirections = new ArrayList<>();
        List<MyMap.Directions> unfavorableDirections = new ArrayList<>();

        this.currentPosition = currentPosition;

        Utils.log("currentPosition: [" + currentPosition.getX() + "," + currentPosition.getY() + "]\n");
        Utils.log("target: [" + target.getX() + "," + target.getY() + "]\n");

        if(target.getX()<currentPosition.getX()) {
            if(checkDirection(MyMap.Directions.WEST)) {
                advantageousDirections.add(MyMap.Directions.WEST);
            }
            if(checkDirection(MyMap.Directions.EAST)) {
                unfavorableDirections.add(MyMap.Directions.EAST);
            }
        }
        else if(target.getX()>currentPosition.getX()) {
            if(checkDirection(MyMap.Directions.WEST)) {
                unfavorableDirections.add(MyMap.Directions.WEST);
            }
            if(checkDirection(MyMap.Directions.EAST)) {
                advantageousDirections.add(MyMap.Directions.EAST);
            }
        }
        else {
            if(checkDirection(MyMap.Directions.WEST)) {
                unfavorableDirections.add(MyMap.Directions.WEST);
            }
            if(checkDirection(MyMap.Directions.EAST)) {
                unfavorableDirections.add(MyMap.Directions.EAST);
            }
        }

        if(target.getY()<currentPosition.getY()) {
            if(checkDirection(MyMap.Directions.NORTH)) {
                advantageousDirections.add(MyMap.Directions.NORTH);
            }
            if(checkDirection(MyMap.Directions.SOUTH)) {
                unfavorableDirections.add(MyMap.Directions.SOUTH);
            } 
        }
        else if(target.getY()>currentPosition.getY()) {
            if(checkDirection(MyMap.Directions.NORTH)) {
                unfavorableDirections.add(MyMap.Directions.NORTH);
            }
            if(checkDirection(MyMap.Directions.SOUTH)) {
                advantageousDirections.add(MyMap.Directions.SOUTH);
            } 
        }
        else {
            if(checkDirection(MyMap.Directions.NORTH)) {
                unfavorableDirections.add(MyMap.Directions.NORTH);
            }
            if(checkDirection(MyMap.Directions.SOUTH)) {
                unfavorableDirections.add(MyMap.Directions.SOUTH);
            } 
        }

        for(MyMap.Directions direction: advantageousDirections) {
            Utils.log("advantageous direction: " + direction + "\n");
        }
        for(MyMap.Directions direction: unfavorableDirections) {
            Utils.log("unfavorable direction: " + direction + "\n");
        }
        for(Field  field: doNotEnterFields) {
            Utils.log("do not enter fields: [" + field.getX() + ", " + field.getY() + "]\n");
        }

        if (!advantageousDirections.isEmpty()) {
            MyMap.Directions direction = advantageousDirections.get(Utils.RNG.nextInt(advantageousDirections.size()));
            Utils.log("chosen direction: "+direction);
            return direction;
        }
        else if (!unfavorableDirections.isEmpty()) {
            //doNotEnterFields.add(getMyMap().getField(currentPosition));
            MyMap.Directions direction = unfavorableDirections.get(Utils.RNG.nextInt(unfavorableDirections.size()));
            Utils.log("chosen direction: "+direction);
            return direction;
        } 
        else {
            Utils.log("chosen direction: keine");
            return null;
        }
    }

    @Override
    void reset() {
        doNotEnterFields.clear();
    }
    
}
