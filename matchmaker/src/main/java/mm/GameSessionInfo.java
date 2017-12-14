package mm;

import java.util.ArrayList;

public class GameSessionInfo {

    private Long gameId;
    ArrayList<String> players = new ArrayList<>();

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}
