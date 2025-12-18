import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;

public class Pathfinder {
    
    private MyMap map;

    public Pathfinder(MyMap map) {
        this.map = map;
    }

    // breadth-first search
    public Stack<MyMap.Directions> findPathBFS(Field start, Field target) {
        Predecessor[][] predecessor = new Predecessor[map.getWidth()][map.getHeight()];
        boolean[][] visited = new boolean[map.getWidth()][map.getHeight()];
        Queue<Field> queue = new ArrayDeque<Field>();
        Stack<MyMap.Directions> path = new Stack<>();
        
        predecessor[start.getX()][start.getY()] = null;
        visited[start.getX()][start.getY()] = true;
        queue.add(start);

        //Utils.log("find path with bfs!\nsize of queue: " + queue.size() + "\npredecessor[target.getX()][target.getY()]:" + predecessor[target.getX()][target.getY()] +  "\n");
        while(!queue.isEmpty() && predecessor[target.getX()][target.getY()] == null) {
            Field currentField = queue.poll();
/*             if(currentField!=null) {
                Utils.log("currentField: [" + currentField.getX() + "; " + currentField.getY() + "]");
            }
            else {
               Utils.log("currentField is null!"); 
            } */
            // check neighbours
            for(MyMap.Directions direction: MyMap.Directions.values()) {
                Field floorNeighbour = map.getFloorNeighbours(currentField, direction);
                if(floorNeighbour!=null && !visited[floorNeighbour.getX()][floorNeighbour.getY()]) {
                    predecessor[floorNeighbour.getX()][floorNeighbour.getY()] = new Predecessor(direction, currentField);
                    visited[floorNeighbour.getX()][floorNeighbour.getY()] = true;
                    queue.add(floorNeighbour);
                    /* Utils.log("added floorNeighbour to queue: ["
                     + floorNeighbour.getX() + "; " + floorNeighbour.getY() + "]"
                     + "size of queue: " + queue.size() + "\n"); */
                }
            }
        }

        //Utils.log("predecessor[target.getX()][target.getY()]: " + predecessor[target.getX()][target.getY()]);
        if(predecessor[target.getX()][target.getY()] != null) {
            Field field = target;
            Predecessor predecessorField;
            while(!field.equals(start)) {
                predecessorField = predecessor[field.getX()][field.getY()];
                path.push(predecessorField.getDirection());
                field = predecessorField.getField();
                /* Utils.log(predecessorField.getOppositeDirection() + " to [" + predecessorField.getField().getX() + "; "
                 + predecessorField.getField().getY() + "]"); */
            }
        }

/*         Utils.log("size of path: " + path.size()); 
        for(int i=path.size()-1; i>=0; i--) {
            Utils.log("Schritt: " + path.get(i));
        } */

        return path;
    }

    private class Predecessor {
        private Field field;
        private MyMap.Directions direction;

        public Predecessor(MyMap.Directions direction, Field field) {
            this.direction = direction;
            this.field = field;
        }

        public Field getField() {
            return field;
        }

        public MyMap.Directions getDirection() {
            return direction;
        }

        public MyMap.Directions getOppositeDirection() {
            if(direction==MyMap.Directions.NORTH) {
                return MyMap.Directions.SOUTH;
            }
            else if(direction==MyMap.Directions.EAST) {
                return MyMap.Directions.WEST;
            }
            else if(direction==MyMap.Directions.SOUTH) {
                return MyMap.Directions.NORTH;
            }
            else if(direction==MyMap.Directions.WEST) {
                return MyMap.Directions.EAST;
            }
            else {
                return null;
            }
        }

    }

}
