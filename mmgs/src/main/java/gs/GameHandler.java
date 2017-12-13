package gs;

import gs.gamerepository.GameController;
import gs.replicator.Replicator;
import gs.network.Broker;
import gs.network.ConnectionPool;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameHandler extends TextWebSocketHandler implements WebSocketHandler {


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        System.out.println("Socket Connected: " + session);
        String str = session.getUri().toString();
        String name = str.substring(str.indexOf("&") + 6);
        GameController.getGameMechanics().getGs().addSession(session, name);
        GameController.getGameMechanics().getGs().increasePlayersInGame(name);
        //System.out.println(str.substring(str.indexOf("gameId")+7,str.indexOf("&")));

        ConnectionPool.getInstance().add(session, name);

        //GameController.getGameMechanics().run();

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // System.out.println("Received " + message.toString());
        String str = message.getPayload().toString();
        //System.out.println(str);
        //System.out.println(str.substring(str.indexOf("direction") + 12, str.lastIndexOf("\"")));
        Broker broker = new Broker();
        broker.receive(session, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
//        GameController.getGameMechanics().clear();
        GameController.getGameMechanics().getGs().decreasePlayersInGame();
        if (GameController.getGameMechanics().getGs().getPlayersInGame() == 0) {
            GameController.getGameMechanics().getTicker().setInterrupted(true);
        }
        System.out.println("Socket Closed: [" + closeStatus.getCode() + "] " + closeStatus.getReason());
        super.afterConnectionClosed(session, closeStatus);
    }

}
