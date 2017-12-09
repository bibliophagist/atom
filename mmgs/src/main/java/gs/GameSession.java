package gs;

import gs.model.Bomb;
import gs.model.Explosion;
import gs.model.Pawn;
import gs.model.Wall;
import gs.tick.Tickable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.socket.WebSocketSession;
import gs.geometry.Point;
import gs.model.Wall.Type;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class GameSession implements Tickable {
    private static final Logger log = LogManager.getLogger(GameSession.class);

    private ConcurrentHashMap<Point, Pawn> allPawns = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Point, Wall> allWalls = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Point, Bomb> allBombs = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Point, Explosion> allExplosions = new ConcurrentHashMap<>();

    private static AtomicLong idGenerator = new AtomicLong();

    public static final int PLAYERS_IN_GAME = 2;

    private final int playersInGame;
    private final long iD = idGenerator.getAndIncrement();

    private WebSocketSession session;

    public GameSession(int playerCount) {
        this.playersInGame = playerCount;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public ConcurrentHashMap<Point, Pawn> getAllPawns(){
        return allPawns;
    }

    public ConcurrentHashMap<Point, Bomb> getAllBombs() {
        return allBombs;
    }

    public ConcurrentHashMap<Point, Explosion> getAllExplosions() {
        return allExplosions;
    }

    public ConcurrentHashMap<Point, Wall> getAllWalls() {
        return allWalls;
    }

    public String jsonStringPawns() {
        if (allPawns.size() == 0) {
            return null;
        } else {
            String objjson = "";
            for (Point p : allPawns.keySet()) {
                Pawn obj = allPawns.get(p);
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

    public String jsonStringExplosions() {
        if (allExplosions.size() == 0) {
            return null;
        } else {
            String objjson = "";
            for (Point p : allExplosions.keySet()) {
                Explosion obj = allExplosions.get(p);
                objjson = objjson + obj.toJson() + ",";
            }
            String result = objjson.substring(0, (objjson.length() - 1));
            return result;
        }
    }

    public long getiD() {
        return iD;
    }

    public Point tileToPixel(Point pos) {
        int x1 = 32 * pos.getX();
        int y1 = 32 * pos.getY();
        return new Point(x1, y1);
    }

    public Point pixelToTile(Point pos) {
        int x1 = pos.getX() / 32;
        int y1 = pos.getY() / 32;
        return new Point(x1, y1);
    }

    @Override
    public void tick(long elapsed) {
    }
}
