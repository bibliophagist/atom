package mm;

import mm.dao.Player;
import mm.dao.PlayerDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.*;

@Service
public class Matchmaker implements Runnable {
    private static final Logger log = LogManager.getLogger(Matchmaker.class);

    @Autowired
    GameServiceRequest client = new GameServiceRequest();
    private static final int PLAYER_COUNT = 4;
    private static final int TIMEOUT = 4;
    private final PlayerDao playerDao = new PlayerDao();
    private final BlockingQueue<Player> bronzeQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Player> silverQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Player> goldQueue = new LinkedBlockingQueue<>();
    final Map<String, Long> inGamePlayers = new ConcurrentHashMap<>();
    final ArrayList<GameSessionInfo> runningGames = new ArrayList<>();


    public boolean join(@NotNull String name) {
        int rank = playerDao.getPlayerRank(name);
        log.info("player " + name + " has rank " + rank);
        if (rank < 50)
            return bronzeQueue.offer(new Player(0, name));
        if (rank < 100)
            return silverQueue.offer(new Player(0, name));
        return goldQueue.offer(new Player(0, name));
    }

    @Override
    public void run() {
        new Thread(() -> {
            Thread.currentThread().setName("bronzeQueue");
            gameConstructor(bronzeQueue, "Bronze queue");
        }).start();

        new Thread(() -> {
            Thread.currentThread().setName("silverQueue");
            gameConstructor(silverQueue, "Silver queue");
        }).start();

        new Thread(() -> {
            Thread.currentThread().setName("goldQueue");
            gameConstructor(goldQueue, "Gold queue");
        }).start();
    }

    private void gameConstructor(BlockingQueue<Player> queue, String queueName) {
        ArrayList<Player> players = new ArrayList<>(PLAYER_COUNT);
        long gameId = 0L;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Player newPlayer = queue.poll(TIMEOUT, TimeUnit.SECONDS);
                if (newPlayer == null) {
                    if (players.size() > 1 && players.size() < PLAYER_COUNT) {
                        addToDataBase(players, gameId);
                        log.info("timeout, game started with " +
                                players.size() +
                                " players in " +
                                queueName +
                                ", pushed to DB");
                        client.start(gameId);
                        //TODO: client.start starts a 10 second timer before starting the game
                        players.clear();
                    }
                } else if (inGamePlayers.containsKey(newPlayer.getLogin())) {
                    players.add(newPlayer);
                    log.info("gameConstructor has " + players.size() + " players");
                    if (players.size() == 1) {
                        gameId = Long.parseLong(client.create(PLAYER_COUNT));
                        //gameId++; //FIXME: should be removed when gs works
                        log.info("created new game with gameId = " + gameId);
                    }
                    log.info(newPlayer.getLogin() + " added to table");
                    inGamePlayers.put(newPlayer.getLogin(), gameId);
                }
            } catch (InterruptedException e) {
                log.error("queue interrupted");
            }

            if (players.size() == PLAYER_COUNT) {
                addToDataBase(players, gameId);
                log.info("game started with maximum players in " +
                        queueName +
                        ", pushed to DB");
                client.start(gameId);
                players.clear();
            }
        }
    }

    private void addToDataBase(ArrayList<Player> players, long gameId) {
        GameSessionInfo gameSession = new GameSessionInfo();
        gameSession.setGameId(gameId);
        for (Player player :
                players) {
            player.setGameId(gameId);
            playerDao.insert(player);
            gameSession.players.add(player.getLogin());
        }
    }

}