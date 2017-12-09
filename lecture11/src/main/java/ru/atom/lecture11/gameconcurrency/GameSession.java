package ru.atom.lecture11.gameconcurrency;

public class GameSession {
    private final String gameId;
    private volatile boolean started;

    public GameSession(String gameId) {
        this.gameId = gameId;
    }

    public void start() {
        this.started = true;
    }

    @Override
    public String toString() {
        return "GameSession{" +
                "gameId='" + gameId + '\'' +
                ", started=" + started +
                '}';
    }
}
