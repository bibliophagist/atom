package gs.model;


import gs.tick.Tickable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gs.geometry.Point;

public class Bonus extends Field implements Positionable, Tickable {
    private static final Logger log = LogManager.getLogger(Bomb.class);
    private final int id;
    private Point point;
    private long time;

    public enum Type {
        speed, bomb, fire
    }

    private Type type;

    public Bonus(int x, int y, long time, Bonus.Type type) {
        super(x, y);
        this.id = getId();
        this.type=type;
        this.point = getPosition();
        log.info("Bonusid = " + id + "; " + "Bonus place = (" + point.getX() + "," +
                point.getY() + ")" + "; " + "Bonus timer = " + time);
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void tick(long elapsed) {
        if (time < elapsed) {
            time = 0;
        } else {
            time -= elapsed;
        }
    }

    public String toJson() {
        Point pos = getPosition();
        String obj = "{\"type\":\"" + this.getClass().getSimpleName() + "\",\"id\":" +
                this.getId() + ",\"position\":{\"x\":" + pos.getX() + ",\"y\":" + pos.getY() + "}}";
        return obj;
    }
}