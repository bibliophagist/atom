package gs;

import gs.geometry.Point;
import gs.inputqueue.InputQueue;
import gs.message.Message;
import gs.message.Topic;
import gs.model.Bomb;
import gs.model.Explosion;
import gs.model.Pawn;
import gs.model.Wall;
import gs.GameSession;
import gs.network.ConnectionPool;
import gs.replicator.Replicator;
import gs.tick.Tickable;
import gs.util.JsonHelper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GameMechanics implements Tickable, Runnable {
    private static final Logger log = LogManager.getLogger(GameMechanics.class);
    private static final int TIMEOUT = 10;
    Replicator replicator = new Replicator();

    private GameSession gs = new GameSession(4);//FIXME исправить после изменения matchmaker'a

    public GameSession getGs() {
        return gs;
    }

    public GameMechanics(GameSession gameSession) {
        this.gs = gameSession;
    }

    @Override
    public void run() {
        initCanvas();
        writeReplica(gs,ConnectionPool.getInstance());
    }

    public void clear() {
        while (!InputQueue.getQueue().isEmpty()) {
            InputQueue.getQueue().poll();
        }
    }

    //TODO двигать игрока в указаном направлении
    public void handleMove(Message msg, Pawn pawn) {
        System.out.println("i am moving! " + msg);

    }

    //TODO создавать бомбу по координатам игрока
    public void handleBomb(GameSession gs, Pawn pawn) {
        System.out.println("i am bombing! ");
        Bomb bomb = new Bomb(pawn.getPosition().getX(), pawn.getPosition().getY(), 300);
        gs.getAllBombs().put(new Point(pawn.getPosition().getX(), pawn.getPosition().getY()), bomb);
    }

    //TODO дописать?
    public void writeReplica(GameSession gs, ConnectionPool cp) {
        replicator.writeReplica(gs, cp);
    }

    @Override
    public void tick(long elapsed) {
        for (Point p : gs.getAllBombs().keySet()) {
            Bomb bomb = gs.getAllBombs().get(p);
            if (bomb.isDead()) {
                Wall rightWall = gs.getAllWalls().get(new Point(p.getX() + 1, p.getY()));
                Wall leftWall = gs.getAllWalls().get(new Point(p.getX() - 1, p.getY()));
                Wall topWall = gs.getAllWalls().get(new Point(p.getX(), p.getY() + 1));
                Wall bottomWall = gs.getAllWalls().get(new Point(p.getX(), p.getY() - 1));
                gs.getAllBombs().remove(p);
            }
        }
        for (Point p : gs.getAllExplosions().keySet()) {
            Explosion explosion = gs.getAllExplosions().get(p);
            if (explosion.isDead()) {
                gs.getAllExplosions().remove(p);
            }
        }
    }

    public void initCanvas() {
        //getAllPawns().put(new Point(32, 32), new Pawn(32, 32, 300));
        gs.getAllBombs().put(new Point(40, 24), new Bomb(40, 24, 300));
        gs.getAllPawns().put(new Point(480, 32), new Pawn(480, 32, 300));
        gs.getAllPawns().put(new Point(32, 352), new Pawn(32, 352, 300));
        gs.getAllPawns().put(new Point(480, 352), new Pawn(480, 352, 300));
        for (int i = 0; i < 13; ++i) {
            for (int k = 0; k < 17; ++k) {
                if (i == 0 || i == 12) {
                    gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Wall));
                } else if (i == 1 || i == 11) {
                    if (k == 0 || k == 16) {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Wall));
                    } else if (k == 1 || k == 2 || k == 14 || k == 15) {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                    } else {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Wood));
                    }
                } else if (i == 2 || i == 10) {
                    if (k == 0 || k == 16) {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Wall));
                    } else if (k == 1 || k == 15) {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                    } else {
                        if (k % 2 == 0) {
                            gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Wall));
                        } else {
                            gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Wood));
                        }
                    }
                } else if (i % 2 != 0) {
                    if (k == 0 || k == 16) {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Wall));
                    } else {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Wood));
                    }
                } else {
                    if (k % 2 == 0) {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Wall));
                    } else {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Wood));
                    }
                }
            }
        }
        /*for (Point p : gs.getAllWalls().keySet()){
            System.out.println(gs.getAllWalls().get(p).getId());
        }*/
    }
}

