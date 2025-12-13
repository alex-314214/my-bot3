
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.json.*;   // from json.jar

public class Bot {

  public static int COUNT_TEST_PATHS = 1;
  public static final boolean ENABLE_LOGGING = true;  
  public static final Logger LOGGER = new Logger();

  private Position position;
  private FieldHandler fieldHandler;
  private Set<Gem> gems = new HashSet<>();
  private Gem currentGem = null;
  private Random rng = new Random(1L); 
  private Set<Field> doNotEnterFields = new HashSet<>();
  private List<Field> visibleFloorFields = new ArrayList<>();
  //private List<Field> floorFieldsWithUnkownNeighbours = new LinkedList<>();
  private int foundGems = 0;
  private long tickStartTime = 0;
  private Field scoutingTarget = null;

  public static void main(String[] args) throws Exception {
    try {
      Bot bot = new Bot();
      bot.processData();
    } catch (Exception e) {
        LOGGER.log("Exception: " + e.toString());
        LOGGER.stop();
        throw e;
    }
  }

  private void log(String text) {
    if (ENABLE_LOGGING) {
        LOGGER.log(text);
    }
  }

  private void processData() {
    if (ENABLE_LOGGING) {
        try (FileWriter fw = new FileWriter("./ausgabe.txt", false)) {
            // Datei wird hier geleert
        } catch (IOException ignored) {}
        try {
          LOGGER.start("./ausgabe.txt");
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    BufferedReader br;
    try {
      br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
      boolean firstTick = true;

      String line;
      while (true) {
        tickStartTime = System.currentTimeMillis();
        line = br.readLine();

        JSONObject data = new JSONObject(line);
        JSONArray botJson = data.optJSONArray("bot");
        
        if (firstTick) {
          JSONObject cfg = data.optJSONObject("config");
          int width  = cfg != null ? cfg.optInt("width")  : 0;
          int height = cfg != null ? cfg.optInt("height") : 0;
          int vis_radius = cfg != null ? cfg.optInt("vis_radius") : 0;
          int max_ticks = cfg != null ? cfg.optInt("max_ticks") : 0;
          System.err.println("My Bot (Java) launching on a " + width + "x" + height + " map at [" + botJson.getInt(0) + "," + botJson.getInt(1) +")");
          System.err.println("vis_radius: " + vis_radius);
          System.err.println("max_ticks: " + max_ticks);
          System.err.println("arena width: " + width);
          System.err.println("arena height: " + height);
          
          fieldHandler = new FieldHandler(width, height);
        }
        updateFields(fieldHandler, data.optJSONArray("floor"), data.optJSONArray("wall"));
        botJson = data.optJSONArray("bot");
        position = new Position(botJson.getInt(0), botJson.getInt(1));
        updateGems(data.getJSONArray("visible_gems"));
        //log("visibleGems: " + visibleGems.size());
        log("\ncurrent bot postion: [" + position.getX() + ", " + position.getY() +"]\n");
        //log(fieldHandler.createMap(position));

/*         Field target = fieldHandler.getFloorFieldWithUnkownNeighbours(new Position(1, 4));
        if(target!=null) {
          log("target: [" + target.getX() + ", " + target.getY() +"]");
        } */

        System.out.println("WAIT");
        //FieldHandler.Directions dir = think();
        //go(dir);
      
        firstTick = false;
      }
    } 
    catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    if (ENABLE_LOGGING) {
        LOGGER.stop();
    }

  }
  
  private FieldHandler.Directions think() {
    FieldHandler.Directions direction = null;
    log(fieldHandler.createMap(position));
    // current gem existing
    if(currentGem!=null) {
      direction = determineDirection(currentGem.getField().getPosition());
      scoutingTarget = null;
    }
    else { // current gem not existing
      Field target = scout();
      if(target!=null) {
        direction = determineDirection(target.getPosition());
      }
    }

    log("doNotEnterFields.size(): " + doNotEnterFields.size());

    if(direction==null) {
      Field target = fieldHandler.getRandomFloorField(rng);
      doNotEnterFields.clear();
      direction = determineDirection(target.getPosition());
    }
    return direction;
  }

  private boolean checkDirection(FieldHandler.Directions direction) {
    Field neighbour = fieldHandler.getNeighbour(position, direction);
    if(neighbour.isAccessible() && !doNotEnterFields.contains(neighbour)) {
        return true;
    }
    return false;
  }


  private FieldHandler.Directions determineDirection(Position target) {
    List<FieldHandler.Directions> advantageousDirections = new ArrayList<>();
    List<FieldHandler.Directions> unfavorableDirections = new ArrayList<>();

    log("position: [" + position.getX() + "," + position.getY() + "]\n");
    log("target: [" + target.getX() + "," + target.getY() + "]\n");
    if(target.getX()<position.getX()) {
      if(checkDirection(FieldHandler.Directions.WEST)) {
        advantageousDirections.add(FieldHandler.Directions.WEST);
      }
      if(checkDirection(FieldHandler.Directions.EAST)) {
        unfavorableDirections.add(FieldHandler.Directions.EAST);
      }
    }
    else if(target.getX()>position.getX()) {
      if(checkDirection(FieldHandler.Directions.WEST)) {
        unfavorableDirections.add(FieldHandler.Directions.WEST);
      }
      if(checkDirection(FieldHandler.Directions.EAST)) {
        advantageousDirections.add(FieldHandler.Directions.EAST);
      }
    }
    else {
      if(checkDirection(FieldHandler.Directions.WEST)) {
        unfavorableDirections.add(FieldHandler.Directions.WEST);
      }
      if(checkDirection(FieldHandler.Directions.EAST)) {
        unfavorableDirections.add(FieldHandler.Directions.EAST);
      }
    }

    if(target.getY()<position.getY()) {
      if(checkDirection(FieldHandler.Directions.NORTH)) {
        advantageousDirections.add(FieldHandler.Directions.NORTH);
      }
      if(checkDirection(FieldHandler.Directions.SOUTH)) {
        unfavorableDirections.add(FieldHandler.Directions.SOUTH);
      } 
    }
    else if(target.getY()>position.getY()) {
      if(checkDirection(FieldHandler.Directions.NORTH)) {
        unfavorableDirections.add(FieldHandler.Directions.NORTH);
      }
      if(checkDirection(FieldHandler.Directions.SOUTH)) {
        advantageousDirections.add(FieldHandler.Directions.SOUTH);
      } 
    }
    else {
      if(checkDirection(FieldHandler.Directions.NORTH)) {
        unfavorableDirections.add(FieldHandler.Directions.NORTH);
      }
      if(checkDirection(FieldHandler.Directions.SOUTH)) {
        unfavorableDirections.add(FieldHandler.Directions.SOUTH);
      } 
    }

    StringBuffer sb = new StringBuffer();
    for(FieldHandler.Directions direction: advantageousDirections) {
     log("advantageous direction: " + direction + "\n");
    }
    for(FieldHandler.Directions direction: unfavorableDirections) {
     log("unfavorable direction: " + direction + "\n");
    }
    for(Field  field: doNotEnterFields) {
     log("do not enter fields: [" + field.getX() + ", " + field.getY() + "]\n");
    }

    if (!advantageousDirections.isEmpty()) {
      FieldHandler.Directions direction = advantageousDirections.get(rng.nextInt(advantageousDirections.size()));
      log("chosen direction: "+direction);
      return direction;
    }
    else if (!unfavorableDirections.isEmpty()) {
      doNotEnterFields.add(fieldHandler.getField(position));
      FieldHandler.Directions direction = unfavorableDirections.get(rng.nextInt(unfavorableDirections.size()));
      log("chosen direction: "+direction);
      return direction;
    } 
    else {
        log("chosen direction: keine");
        return null;
    }

  }

  private void go(FieldHandler.Directions direction) {
    if (direction == null) {
        System.out.println("WAIT");
        System.out.flush();
        return;
    }

    switch (direction) {
        case NORTH:
            System.out.println("N");
            break;
        case EAST:
            System.out.println("E");
            break;
        case SOUTH:
            System.out.println("S");
            break;
        case WEST:
            System.out.println("W");
            break;
    }
    System.out.flush();
    log("time: "  + (System.currentTimeMillis()-tickStartTime));
  }

  private Gem getNearestGem() {
    Gem nearestGem = null;
    int stepsMin = 999999999;

    for(Gem gem: gems) {
        int steps = position.getDistance(gem.getField().getPosition());
        if(steps<stepsMin) {
          stepsMin = steps;
          nearestGem = gem;
        }
    }

    return nearestGem;
  }

  public void updateGems(JSONArray visibleGemsJson) {
    //visibleGems.clear();

    // gem found?
    Field botField = fieldHandler.getField(position);
    if(currentGem!=null && currentGem.getField().getPosition().equals(botField.getPosition())) {
      foundGems++;
      log("\nfound gems: " + foundGems + "\n");
      botField.removeGem();
      currentGem = null;
      doNotEnterFields.clear();
    }
    else {
      for(Gem gem: gems) {
        if(botField.getPosition().equals(gem.getField().getPosition())) {
          foundGems++;
          log("\nfound gems: " + foundGems + "\n");
          botField.removeGem();
          gems.remove(gem);
          break;
        }
      }
    }

    // adjust ttl
    for(Gem gem: gems) {
      if(gem.reduceTtl()==null) {
        gems.remove(gem);
      };
    }
    if(currentGem!=null) {
      currentGem.reduceTtl();
      if(currentGem.reduceTtl()==null) {
        currentGem = null;
        doNotEnterFields.clear();
      };
    }

    // adjust gems
    if(visibleGemsJson!=null) {
      for (int i = 0; i < visibleGemsJson.length(); i++) {
          JSONObject jsonGem = visibleGemsJson.getJSONObject(i);
          JSONArray pos = jsonGem.getJSONArray("position");
          int ttl = jsonGem.getInt("ttl");
          int x = pos.getInt(0);
          int y = pos.getInt(1);
          Field field = fieldHandler.getField(x, y);
          Gem gem = field.getGem();
          if(gem==null) {
            gem = new Gem(field, ttl);
          } 
          //visibleGems.add(gem);

          // add gem if it does not already exist 
          boolean exists = false;
          for(Gem g: gems) {
            if(g.getField().getPosition().equals(gem.getField().getPosition())) {
              exists = true;
              break;
            }
          }
          if(!exists) {
            gems.add(gem);
          }
      }
    }

    // no current gem?
    if(currentGem==null) {
      currentGem = getNearestGem();
      gems.remove(currentGem);
      if(currentGem!=null) {
        doNotEnterFields.clear();
      }
    }

    // log
    StringBuffer sb = new StringBuffer("\nGems:\n");
    if(currentGem!=null) {
      sb.append("\ncurrent gem at: [" + currentGem.getField().getPosition().getX() + ", " + currentGem.getField().getPosition().getY() + "] with TTL + " + currentGem.getTtl() + "\n");
    }
    else {
      sb.append("\nno current gem\n");
    }
    sb.append("\ngems set:\n");
    for(Gem gem: gems) {
      sb.append("Gem at [" + gem.getField().getPosition().getX() + ", " + gem.getField().getPosition().getY() + "] with TTL + " + gem.getTtl() + "\n");
    }
    log(sb.toString());
  }

  public void updateFields(FieldHandler fieldHandler, JSONArray floor, JSONArray wall) {
    visibleFloorFields.clear();

    for(int i=0; i<floor.length(); i++) {
      JSONArray position = floor.getJSONArray(i);
      int x = position.getInt(0);
      int y = position.getInt(1);
      Field field = fieldHandler.getField(x, y);
      if(field.getType()==Field.Types.UNKOWN) {
        field = new Field(x, y, Field.Types.FLOOR);
        fieldHandler.addField(field);
        //log("\nvisible floor field: [" + field.getX() + ", " + field.getY() + "]\n");
      }     
      visibleFloorFields.add(field);
      
    } 

    for(int i=0; i<wall.length(); i++) {
      JSONArray position = wall.getJSONArray(i);
      int x = position.getInt(0);
      int y = position.getInt(1);
      Field field = fieldHandler.getField(x, y);
      if(field.getType()==Field.Types.UNKOWN) {
        field = new Field(x, y, Field.Types.WALL);
        fieldHandler.addField(field);
        //log("\nvisible wall field: [" + field.getX() + ", " + field.getY() + "]\n");
      }   
    } 
  }

  private Field scout() {
    Field nearestField = null;
    int shortestDistance = 9999999;
    List<Field> visibleFloorFieldsWithUnkownNeighbours = fieldHandler.getFloorFieldsWithUnkownNeighbours(visibleFloorFields);
    List<Field> floorFieldsWithUnkownNeighbours;
    log("\nscouting:\n");
    log("\nvisibleFloorFields.size(): " + visibleFloorFields.size());
    log("\nvisibleFloorFieldsWithUnkownNeighbours.size(): " + visibleFloorFieldsWithUnkownNeighbours.size() + "\n");
    if(visibleFloorFieldsWithUnkownNeighbours.size()>0) {
      for(Field field: visibleFloorFieldsWithUnkownNeighbours) {
        if(!field.getPosition().equals(position)) {
          //log("\nvisible floor fields with unkown neighbours: [" + field.getX() + ", " + field.getY() + "]\n");
          if(nearestField==null) {
            shortestDistance = position.getDistance(field.getPosition());
            nearestField = field;
          }
          else {
            int distance = position.getDistance(field.getPosition());
            if(shortestDistance>distance) {
              shortestDistance = distance;
              nearestField = field;
            }
          }
        }
      }
      if(nearestField!=null){
        log("\nnearestField: [" + nearestField.getX() + ", " + nearestField.getY() + "] \tshortest distance: " + shortestDistance + "\n");
      }
      scoutingTarget = null;
      return nearestField;
    }
    else  {
/*       floorFieldsWithUnkownNeighbours = fieldHandler.getFloorFieldsWithUnkownNeighbours(position);
      if(floorFieldsWithUnkownNeighbours.size()>0) {
        Field field = floorFieldsWithUnkownNeighbours.get(floorFieldsWithUnkownNeighbours.size()-1);
        if(field.equals(fieldHandler.getField(position))) {
          floorFieldsWithUnkownNeighbours.remove(field);
          if(floorFieldsWithUnkownNeighbours.size()>0) {
            field = floorFieldsWithUnkownNeighbours.get(floorFieldsWithUnkownNeighbours.size()-1);
          }
        }
        log("\nscouting field from floorFieldsWithUnkownNeighbours: [" + field.getX() + ", " + field.getY() + "]\n");
        
        return field;
      } 
      else {
        log("\n scouting Field: null\n");
        return null;
      } */
      Field target = fieldHandler.getFloorFieldWithUnkownNeighbours(position);
      if(target!=null) {
        log("\nscouting to: [" + target.getX() + ", " + target.getY() + "]\n");
      }
      else{
        log("\nscouting to: null");
        target = fieldHandler.getRandomFloorField(rng);
      }
      scoutingTarget = target;
      return target;
    }    
  }
}
