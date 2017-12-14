package gs;

import gs.model.Bonus;
import gs.model.Wall;
import gs.model.Bomb;
import gs.model.Fire;
import gs.model.Pawn;
import gs.model.Movable;
import gs.tick.Tickable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.socket.WebSocketSession;
import gs.geometry.Point;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class GameSession implements Tickable {
    private static final Logger log = LogManager.getLogger(GameSession.class);

    private final ConcurrentHashMap<String, Pawn> allPawns = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Point, Wall> allWalls = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Point, Bomb> allBombs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Point, Fire> allFire = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Point, Bonus> allBonuses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WebSocketSession> allSessions = new ConcurrentHashMap<>();

    private static AtomicLong idGenerator = new AtomicLong();
    private int playersInGame = 0;
    private boolean gameSessionIsOver = false;

    private final long iD = idGenerator.getAndIncrement();

    public void increasePlayersInGame(String name) {
        playersInGame++;
        if (playersInGame == 1) {
            allPawns.put(name, new Pawn(32, 32, 32));
        } else if (playersInGame == 2) {
            allPawns.put(name, new Pawn(480, 32, 32));
        } else if (playersInGame == 3) {
            allPawns.put(name, new Pawn(32, 352, 32));
        } else if (playersInGame == 4) {
            allPawns.put(name, new Pawn(480, 352, 32));
        } else {
            System.out.println("Wrong number of players!");
        }
    }

    public void decreasePlayersInGame() {
        playersInGame--;
    }

    public int getPlayersInGame() {
        return playersInGame;
    }

    public void addSession(WebSocketSession session, String player) {
        allSessions.put(player, session);
    }

    public String jsonStringPawns() {
        if (allPawns.size() == 0) {
            return null;
        } else {
            String objjson = "";
            for (String name : allPawns.keySet()) {
                Pawn obj = allPawns.get(name);
                objjson = objjson + obj.toJson() + ",";
            }
            String result = objjson.substring(0, (objjson.length() - 1));
            return result;
        }
    }

    public String jsonStringWalls() {
        String objjson = "";
        for (Point p : allWalls.keySet()) {
            Wall obj = allWalls.get(p);
            objjson = objjson + obj.toJson() + ",";
        }
        String result = objjson.substring(0, (objjson.length() - 1));
        return result;
    }

    public String jsonStringBombs() {
        if (allBombs.size() == 0) {
            return null;
        } else {
            String objjson = "";
            for (Point p : allBombs.keySet()) {
                Bomb obj = allBombs.get(p);
                objjson = objjson + obj.toJson() + ",";
            }
            String result = objjson.substring(0, (objjson.length() - 1));
            return result;
        }
    }

    public String jsonStringBonuses() {
        if (allBonuses.size() == 0) {
            return null;
        } else {
            String objjson = "";
            for (Point p : allBonuses.keySet()) {
                Bonus obj = allBonuses.get(p);
                objjson = objjson + obj.toJson() + ",";
            }
            String result = objjson.substring(0, (objjson.length() - 1));
            return result;
        }
    }

    public String jsonStringExplosions() {
        if (allFire.size() == 0) {
            return null;
        } else {
            String objjson = "";
            for (Point p : allFire.keySet()) {
                Fire obj = allFire.get(p);
                objjson = objjson + obj.toJson() + ",";
            }
            String result = objjson.substring(0, (objjson.length() - 1));
            return result;
        }
    }

    public long getiD() {
        return iD;
    }

    public void kilGameSession() {
        this.gameSessionIsOver = true;
    }

    public boolean isGameSessionIsOver() {
        return gameSessionIsOver;
    }

    public WebSocketSession getSession(String name) {
        return allSessions.get(name);
    }

    public ConcurrentHashMap<String, WebSocketSession> getAllSessions() {
        return allSessions;
    }

    public ConcurrentHashMap<String, Pawn> getAllPawns() {
        return allPawns;
    }

    public ConcurrentHashMap<Point, Bomb> getAllBombs() {
        return allBombs;
    }

    public ConcurrentHashMap<Point, Fire> getAllFire() {
        return allFire;
    }

    public ConcurrentHashMap<Point, Wall> getAllWalls() {
        return allWalls;
    }

    public ConcurrentHashMap<Point, Bonus> getAllBonuses() {
        return allBonuses;
    }

    @Override
    public void tick(long elapsed) {
    }
}
