
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.json.*;   // from json.jar

public class Bot {

  public static int COUNT_TEST_PATHS = 1;

  private Position position;
  private MyMap map;
  private Set<Gem> gems = new HashSet<>();
  private Gem currentGem = null;
  private boolean newCurrentGem = false;
  private Random rng = new Random(1L); 
  private List<Field> visibleFloorFields = new ArrayList<>();
  private int foundGems = 0;
  private Field target = null;
  private Tactic tactic;
  private Stack<MyMap.Directions> path = new Stack<>();
  Pathfinder pathfinder;

  public static void main(String[] args) throws Exception {
    Bot bot = new Bot();
    bot.processData();
  }

  private void processData() {
    Utils.enableLogging();
    
    BufferedReader br;
    try {
      br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
      boolean firstTick = true;

      String line;
      while (true) {
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
          
          map = new MyMap(width, height);
          pathfinder = new Pathfinder(map);
          tactic = new PreferedDirection(map);
        }

        updateFields(map, data.optJSONArray("floor"), data.optJSONArray("wall"));
        
        botJson = data.optJSONArray("bot");
        position = new Position(botJson.getInt(0), botJson.getInt(1));
        Utils.log("\ncurrent bot postion: [" + position.getX() + ", " + position.getY() +"]\n");
        
        updateGems(data.getJSONArray("visible_gems"));

        //System.out.println("WAIT");
        MyMap.Directions dir = think();
        go(dir);

        firstTick = false;
         Utils.log("\n----------------------------------------------------------");
      }
    } 
    catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    Utils.disableLogging();
  }
  
  private MyMap.Directions think() {
    MyMap.Directions direction = null;
    String msg;
    Utils.log(map.createMap(position));
    
    // if currentGem existing go for it otherwise scout
    if(newCurrentGem) {
      target = currentGem.getField();
      msg = "new path to new gem! steps: "; 
    }
    else if(path.size()==0) {
      target = scout();
      msg = "new path for scouting! steps: ";
    }
    else {
      msg = "UNEXPECTED! ";
    }
    path = pathfinder.findPathBFS(map.getField(position), target);
    Utils.log(msg + path.size());
    
    if(!path.isEmpty()) {
      direction = path.pop();
    }
    
    Utils.log("\ntarget: [" + target.getX() + ", " + target.getY() + "]");
    Utils.log("\ndirection: " + direction);

    return direction;
  }

  private MyMap.Directions determineDirection(Position target) {
    return tactic.determineDirection(position, target);
  }

  private void go(MyMap.Directions direction) {
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
    Field botField = map.getField(position);
    Gem foundGem = null;
    Gem formerCurrentGem = currentGem;
    LinkedList<Gem> gemsToBeRemoved = new LinkedList<>();

    // check known gems
    for(Gem gem: gems) {
      // gem found?
      if(botField.getPosition().equals(gem.getField().getPosition())) {
        foundGem = gem;
      }

      // adjust ttl
      if(gem.reduceTtl()==null) {
        gemsToBeRemoved.add(gem);
      };
    }

    for(Gem gem: gemsToBeRemoved) {
      gems.remove(gem);
    }

    // remove gem if found
    if(foundGem!=null) {
      foundGems++;
      Utils.log("\nfound gems: " + foundGems + "\n");
      botField.removeGem();
      gems.remove(foundGem);
      if(currentGem==foundGem) {
        currentGem=null;
      }
    }
    
    // add visiblie gems if they are not already known
    if(visibleGemsJson!=null) {
      for (int i = 0; i < visibleGemsJson.length(); i++) {
          JSONObject jsonGem = visibleGemsJson.getJSONObject(i);
          JSONArray pos = jsonGem.getJSONArray("position");
          int ttl = jsonGem.getInt("ttl");
          int x = pos.getInt(0);
          int y = pos.getInt(1);
          Field field = map.getField(x, y);
         
          // new gem?
          if(field.getGem()==null) {
            field.setGem(new Gem(field, ttl));
            gems.add(field.getGem());
          } 
      }
    }

    // select currentGem
    Gem nearestGem = getNearestGem();
    if(currentGem!=nearestGem) {
      currentGem = nearestGem;
      tactic.reset();
    }

    newCurrentGem = false;
    if(currentGem!=formerCurrentGem && currentGem!=null) {
        newCurrentGem = true;
    }

    // Utils.log
    if (Utils.isLoggingEnabled()) {
      StringBuffer sb = new StringBuffer("Gems:");
      if(currentGem!=null) {
        sb.append(" current gem at: [" + currentGem.getField().getPosition().getX() + ", " + currentGem.getField().getPosition().getY() + "] with TTL + " + currentGem.getTtl());
      }
      else {
        sb.append(" no current gem");
      }
      sb.append("\ngems set:\n");
      for(Gem gem: gems) {
        sb.append(" - Gem at [" + gem.getField().getPosition().getX() + ", " + gem.getField().getPosition().getY() + "] with TTL + " + gem.getTtl() + "\n");
      }
      Utils.log(sb.toString());
    }
  }

  public void updateFields(MyMap map, JSONArray floor, JSONArray wall) {
    visibleFloorFields.clear();

    for(int i=0; i<floor.length(); i++) {
      JSONArray position = floor.getJSONArray(i);
      int x = position.getInt(0);
      int y = position.getInt(1);
      Field field = map.getField(x, y);
      if(field.getType()==Field.Types.UNKOWN) {
        field = new Field(x, y, Field.Types.FLOOR);
        map.addField(field);
        //Utils.log("\nvisible floor field: [" + field.getX() + ", " + field.getY() + "]\n");
      }     
      visibleFloorFields.add(field);
    } 

    for(int i=0; i<wall.length(); i++) {
      JSONArray position = wall.getJSONArray(i);
      int x = position.getInt(0);
      int y = position.getInt(1);
      Field field = map.getField(x, y);
      if(field.getType()==Field.Types.UNKOWN) {
        field = new Field(x, y, Field.Types.WALL);
        map.addField(field);
        //Utils.log("\nvisible wall field: [" + field.getX() + ", " + field.getY() + "]\n");
      }   
    } 
  }

  private Field scout() {
    Utils.log("\nscouting:");
    Field target = map.getFloorFieldWithUnkownNeighbours(position);

    if(target!=null) {
      Utils.log(" scouting nearest floor field with unkown neighbours: [" + target.getX() + ", " + target.getY() + "]\n");
    }
    else{
      Utils.log(" random scouting\n");
      target = map.getRandomFloorField(rng);
    }

    return target;
  }    

}
