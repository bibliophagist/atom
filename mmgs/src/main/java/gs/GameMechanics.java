package gs;

import gs.geometry.Point;
import gs.inputqueue.InputQueue;
import gs.message.Message;
import gs.model.Bomb;
import gs.model.Fire;
import gs.model.Pawn;
import gs.model.Wall;
import gs.replicator.Replicator;
import gs.tick.Tickable;
import gs.tick.Ticker;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class GameMechanics implements Tickable, Runnable {
    private static final Logger log = LogManager.getLogger(GameMechanics.class);
    private static final int TIMEOUT = 10;
    private final Replicator replicator = new Replicator();
    private Ticker ticker = new Ticker();

    private GameSession gs = new GameSession(4);//FIXME исправить после изменения matchmaker'a

    public GameSession getGs() {
        return gs;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public GameMechanics(GameSession gameSession) {
        this.gs = gameSession;
    }

    @Override
    public void run() {
        initCanvas();
        ticker.gameLoop();
    }

    //TODO может чистить больше?
    public void clear() {
        while (!InputQueue.getQueue().isEmpty()) {
            InputQueue.getQueue().poll();
        }
    }

    //TODO двигать игрока в указаном направлении
    public void handleMove(Message msg, Pawn pawn) {
        System.out.println("i am moving! " + msg);

    }

    public void handleBomb(GameSession gs, Pawn pawn) {
        System.out.println("i am bombing! ");
        Bomb bomb = new Bomb(pawn.getPosition().getX(), pawn.getPosition().getY(), 300);
        if (pawn.isPowerful()) {
            bomb.setPower(2);
        }
        gs.getAllBombs().put(new Point(pixelToTile(pawn.getPosition()).getX(), pixelToTile(pawn.getPosition()).getY()), bomb);
    }

    public void writeReplica(GameSession gs) {
        replicator.writeReplica(gs);
    }

    @Override
    public void tick(long elapsed) {
        writeReplica(gs);
        System.out.println("Kappa");
        for (Point p : gs.getAllBombs().keySet()) {
            Bomb bomb = gs.getAllBombs().get(p);
            bomb.tick(elapsed);
            if (bomb.isDead()) {
                Wall rightWall = gs.getAllWalls().get(new Point(p.getX() + 1, p.getY()));
                Wall leftWall = gs.getAllWalls().get(new Point(p.getX() - 1, p.getY()));
                Wall topWall = gs.getAllWalls().get(new Point(p.getX(), p.getY() + 1));
                Wall bottomWall = gs.getAllWalls().get(new Point(p.getX(), p.getY() - 1));
                if (rightWall.getType() == Wall.Type.Wood || rightWall.getType() == Wall.Type.Grass) {
                    rightWall.setType(Wall.Type.Grass);
                    gs.getAllFire().put(rightWall.getPosition(), new Fire(tileToPixel(rightWall.getPosition()).getX(), tileToPixel(rightWall.getPosition()).getY()));
                    if (bomb.getPower() == 2) {
                        Wall secondRightWall = gs.getAllWalls().get(new Point(rightWall.getPosition().getX() + 1, rightWall.getPosition().getY()));
                        if (secondRightWall.getType() == Wall.Type.Wood) {
                            secondRightWall.setType(Wall.Type.Grass);
                            gs.getAllFire().put(secondRightWall.getPosition(), new Fire(tileToPixel(secondRightWall.getPosition()).getX(), tileToPixel(secondRightWall.getPosition()).getY()));
                        }
                    }
                }
                if (leftWall.getType() == Wall.Type.Wood || leftWall.getType() == Wall.Type.Grass) {
                    leftWall.setType(Wall.Type.Grass);
                    gs.getAllFire().put(tileToPixel(leftWall.getPosition()), new Fire(tileToPixel(leftWall.getPosition()).getX(), tileToPixel(leftWall.getPosition()).getY()));
                    if (bomb.getPower() == 2) {
                        Wall secondLeftWall = gs.getAllWalls().get(new Point(rightWall.getPosition().getX() - 1, rightWall.getPosition().getY()));
                        if (secondLeftWall.getType() == Wall.Type.Wood) {
                            secondLeftWall.setType(Wall.Type.Grass);
                            gs.getAllFire().put(secondLeftWall.getPosition(), new Fire(tileToPixel(secondLeftWall.getPosition()).getX(), tileToPixel(secondLeftWall.getPosition()).getY()));
                        }
                    }
                }
                if (topWall.getType() == Wall.Type.Wood || topWall.getType() == Wall.Type.Grass) {
                    topWall.setType(Wall.Type.Grass);
                    gs.getAllFire().put(topWall.getPosition(), new Fire(tileToPixel(topWall.getPosition()).getX(), tileToPixel(topWall.getPosition()).getY()));
                    if (bomb.getPower() == 2) {
                        Wall secondTopWall = gs.getAllWalls().get(new Point(rightWall.getPosition().getX(), rightWall.getPosition().getY() + 1));
                        if (secondTopWall.getType() == Wall.Type.Wood) {
                            secondTopWall.setType(Wall.Type.Grass);
                            gs.getAllFire().put(secondTopWall.getPosition(), new Fire(tileToPixel(secondTopWall.getPosition()).getX(), tileToPixel(secondTopWall.getPosition()).getY()));
                        }
                    }
                }
                if (bottomWall.getType() == Wall.Type.Wood || bottomWall.getType() == Wall.Type.Grass) {
                    bottomWall.setType(Wall.Type.Grass);
                    gs.getAllFire().put(bottomWall.getPosition(), new Fire(tileToPixel(bottomWall.getPosition()).getX(), tileToPixel(bottomWall.getPosition()).getY()));
                    if (bomb.getPower() == 2) {
                        Wall secondBottomWall = gs.getAllWalls().get(new Point(rightWall.getPosition().getX(), rightWall.getPosition().getY() - 1));
                        if (secondBottomWall.getType() == Wall.Type.Wood) {
                            secondBottomWall.setType(Wall.Type.Grass);
                            gs.getAllFire().put(secondBottomWall.getPosition(), new Fire(tileToPixel(secondBottomWall.getPosition()).getX(), tileToPixel(secondBottomWall.getPosition()).getY()));
                        }
                    }
                }
                gs.getAllFire().put(pixelToTile(bomb.getPosition()), new Fire(bomb.getPosition().getX(), bomb.getPosition().getY()));
                gs.getAllBombs().remove(p);
            }
        }
        for (Point p : gs.getAllFire().keySet()) {
            Fire fire = gs.getAllFire().get(p);
            fire.tick(elapsed);
            if (fire.isDead()) {
                gs.getAllFire().remove(p);
            }
        }
    }

    public void initCanvas() {
        //getAllPawns().put(new Point(32, 32), new Pawn(32, 32, 300));
        gs.getAllBombs().put(pixelToTile(new Point(64, 32)), new Bomb(64, 32, 300));//TODO ставить красиво (сейчас не в центре ячейки)
        gs.getAllBombs().get(pixelToTile(new Point(64, 32))).setPower(2);
        gs.getAllPawns().put(pixelToTile(new Point(480, 32)), new Pawn(480, 32, 300));
        gs.getAllPawns().put(pixelToTile(new Point(32, 352)), new Pawn(32, 352, 300));
        gs.getAllPawns().put(pixelToTile(new Point(480, 352)), new Pawn(480, 352, 300));
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
//        for (Point p : gs.getAllWalls().keySet()){
//            System.out.println(gs.getAllWalls().get(p).getId());
//        }
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
}

