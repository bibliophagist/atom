package gs;

import gs.replicator.Replicator;
import gs.network.Broker;
import gs.network.ConnectionPool;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GameHandler extends TextWebSocketHandler implements WebSocketHandler {

    private GameSession gs = new GameSession(4);
    private GameMechanics gameMechanics = new GameMechanics(gs);//TODO удалить после реализации Tick
    private final ConnectionPool connectionPool;
    private WebSocketSession webSocketSession;

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    public GameHandler() {
        this.connectionPool = ConnectionPool.getInstance();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        System.out.println("Socket Connected: " + session);
        String str = session.getUri().toString();
        this.webSocketSession = session;
        //System.out.println(str.substring(str.indexOf("gameId")+7,str.indexOf("&")));
        String name = str.substring(str.indexOf("&") + 6);

        ConnectionPool.getInstance().add(session, name);

        gameMechanics.getGs().setSession(session);
        gameMechanics.run();

        /*gameMechanics.initCanvas();
        gameMechanics.getGs().setSession(session);
        gameMechanics.writeReplica(gameMechanics.getGs(), ConnectionPool.getInstance());*/

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // System.out.println("Received " + message.toString());
        String str = message.getPayload().toString();
        System.out.println(str.substring(str.indexOf("direction") + 12, str.lastIndexOf("\"")));
        Broker broker = new Broker();
        broker.receive(session, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        gameMechanics.clear();
        System.out.println("Socket Closed: [" + closeStatus.getCode() + "] " + closeStatus.getReason());
        super.afterConnectionClosed(session, closeStatus);
    }

}
