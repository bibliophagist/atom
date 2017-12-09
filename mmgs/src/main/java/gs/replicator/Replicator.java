package gs.replicator;

import gs.GameSession;
import gs.message.Topic;
import gs.network.Broker;
import gs.network.ConnectionPool;
import org.springframework.web.socket.WebSocketSession;

public class Replicator {

    public void writeReplica(GameSession gs, ConnectionPool connectionPool) {
        WebSocketSession session = gs.getSession();
        if (gs.jsonStringBombs() == null) {
            if (gs.jsonStringExplosions() == null) {
                Broker.getInstance().send(gs, connectionPool.getPlayer(session), Topic.REPLICA, gs.jsonStringWalls() +
                        "," + gs.jsonStringPawns());
            } else {
                Broker.getInstance().send(gs, connectionPool.getPlayer(session), Topic.REPLICA, gs.jsonStringWalls() +
                        "," + gs.jsonStringExplosions() + "," + gs.jsonStringPawns());
            }
        } else if (gs.jsonStringExplosions() == null) {
            Broker.getInstance().send(gs, connectionPool.getPlayer(session), Topic.REPLICA, gs.jsonStringWalls() +
                    "," + gs.jsonStringBombs() + "," + gs.jsonStringPawns());
        } else {
            Broker.getInstance().send(gs, connectionPool.getPlayer(session), Topic.REPLICA, gs.jsonStringWalls() +
                    "," + gs.jsonStringBombs() + "," + gs.jsonStringExplosions() + "," + gs.jsonStringPawns());
        }
    }
}
