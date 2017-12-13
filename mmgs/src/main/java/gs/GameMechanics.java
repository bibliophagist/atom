package gs;

import gs.geometry.Point;
import gs.inputqueue.InputQueue;
import gs.message.Message;
import gs.message.Topic;
import gs.model.*;
import gs.replicator.Replicator;
import gs.tick.Tickable;
import gs.tick.Ticker;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class GameMechanics implements Tickable, Runnable {
    private static final Logger log = LogManager.getLogger(GameMechanics.class);
    private static final int TIMEOUT = 10;
    private final Replicator replicator = new Replicator();
    private Ticker ticker = new Ticker();
    private final ConcurrentHashMap<String, Boolean> bombHasBeenPlanted = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> moveHasBeenMade = new ConcurrentHashMap<>();
    private int currentNumberOfBonuses = 0;

    private GameSession gs = new GameSession();

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

    private void clear() {
        for (String name : gs.getAllPawns().keySet()) {
            bombHasBeenPlanted.put(name, false);
            moveHasBeenMade.put(name, false);
        }
        while (!InputQueue.getQueue().isEmpty()) {
            Message message = InputQueue.getQueue().poll();
            if (message.getTopic().equals(Topic.MOVE) && !moveHasBeenMade.get(message.getOwner())) {
                handleMove(message, gs.getAllPawns().get(message.getOwner()));
                moveHasBeenMade.put(message.getOwner(), true);
            } else if (message.getTopic().equals(Topic.PLANT_BOMB) && !bombHasBeenPlanted.get(message.getOwner())) {
                handleBomb(gs.getAllPawns().get(message.getOwner()));
                bombHasBeenPlanted.put(message.getOwner(), true);
            }
        }
        bombHasBeenPlanted.clear();
        moveHasBeenMade.clear();
    }

    private void handleMove(Message msg, Pawn pawn) {
        if (msg.getData().equals("{\"direction\":\"RIGHT\"}")) {
            Point right = pawn.move(Movable.Direction.RIGHT, 1);
            if (gs.getAllWalls().get(pixelToTile(right)).getType() != Wall.Type.Grass) {
                pawn.move(Movable.Direction.LEFT, 1);
            }
        } else if (msg.getData().equals("{\"direction\":\"LEFT\"}")) {
            Point left = pawn.move(Movable.Direction.LEFT, 1);
            if (gs.getAllWalls().get(pixelToTile(left)).getType() != Wall.Type.Grass) {
                pawn.move(Movable.Direction.RIGHT, 1);
            }
        } else if (msg.getData().equals("{\"direction\":\"UP\"}")) {
            Point up = pawn.move(Movable.Direction.UP, 1);
            if (gs.getAllWalls().get(pixelToTile(up)).getType() != Wall.Type.Grass) {
                pawn.move(Movable.Direction.DOWN, 1);
            }
        } else if (msg.getData().equals("{\"direction\":\"DOWN\"}")) {
            Point down = pawn.move(Movable.Direction.DOWN, 1);
            if (gs.getAllWalls().get(pixelToTile(down)).getType() != Wall.Type.Grass) {
                pawn.move(Movable.Direction.UP, 1);
            }
        } else {
            log.info("Wrong direction or bad message!");
        }
    }

    private void handleBomb(Pawn pawn) {
        Bomb bomb = new Bomb(pawn.getPoint().getX(), pawn.getPoint().getY(), 300);
        if (pawn.isPowerful()) {
            bomb.setPower(2);
        }
        gs.getAllBombs().put(new Point(pixelToTile(pawn.getPoint()).getX(), pixelToTile(pawn.getPoint()).getY()), bomb);//TODO ставить красиво (сейчас не в центре ячейки)
    }

    private void writeReplica(GameSession gs) {
        replicator.writeReplica(gs);
    }

    @Override
    public void tick(long elapsed) {
        clear();
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
        for (String name : gs.getAllPawns().keySet()) {//FIXME dying animation?
            if (gs.getAllFire().containsKey(pixelToTile(gs.getAllPawns().get(name).getPoint()))) {
                gs.getAllPawns().get(name).setPoint(-100, -100);
                try {
                    gs.getAllSessions().get(name).close();
                    gs.getAllSessions().remove(name);
                } catch (IOException e) {
                    log.info("Pawn died, but session can't be closed!");
                }
            }
        }
        for (Point p : gs.getAllFire().keySet()) {
            Fire fire = gs.getAllFire().get(p);
            fire.tick(elapsed);
            if (fire.isDead()) {
                gs.getAllFire().remove(p);
            }
        }
        for (Point p : gs.getAllBonuses().keySet()) {
            gs.getAllBonuses().get(p).tick(elapsed);
        }
        for (String name : gs.getAllPawns().keySet()) {
            if (gs.getAllBonuses().containsKey(pixelToTile(gs.getAllPawns().get(name).getPoint()))) {
                //TODO применить бонус
                gs.getAllBonuses().remove(pixelToTile(gs.getAllPawns().get(name).getPoint()));
            }
        }
        if (currentNumberOfBonuses <= 2) {
            bonusCreator();
        }
        writeReplica(gs);
    }

    private void initCanvas() {
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

        /*for (int i = 0; i < 13; ++i) {
            for (int k = 0; k < 17; ++k) {
                if (i == 0 || i == 12) {
                    gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                } else if (i == 1 || i == 11) {
                    if (k == 0 || k == 16) {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                    } else if (k == 1 || k == 2 || k == 14 || k == 15) {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                    } else {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                    }
                } else if (i == 2 || i == 10) {
                    if (k == 0 || k == 16) {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                    } else if (k == 1 || k == 15) {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                    } else {
                        if (k % 2 == 0) {
                            gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                        } else {
                            gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                        }
                    }
                } else if (i % 2 != 0) {
                    if (k == 0 || k == 16) {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                    } else {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                    }
                } else {
                    if (k % 2 == 0) {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                    } else {
                        gs.getAllWalls().put(new Point(k, i), new Wall(k, i, Wall.Type.Grass));
                    }
                }
            }
        }*/
    }

    private void bonusCreator() {
        Random rnd = new Random(System.currentTimeMillis());
        int randomX = 1 + rnd.nextInt(16);
        int randomY = 1 + rnd.nextInt(12);
        int randomBonusType = rnd.nextInt(2);
        Point position = new Point(randomX, randomY);
        if (gs.getAllWalls().get(position).getType() == Wall.Type.Grass) {
            switch (randomBonusType) {
                case 0:
                    gs.getAllBonuses().put(position, new Bonus(position.getX(), position.getY(), Bonus.Type.speed));
                case 1:
                    gs.getAllBonuses().put(position, new Bonus(position.getX(), position.getY(), Bonus.Type.bomb));
                case 2:
                    gs.getAllBonuses().put(position, new Bonus(position.getX(), position.getY(), Bonus.Type.fire));
            }
        }
        currentNumberOfBonuses++;
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

