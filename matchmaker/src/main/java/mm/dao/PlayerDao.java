package mm.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerDao implements Dao<Player> {
    private static final Logger log = LogManager.getLogger(PlayerDao.class);

    @Language("sql")
    private static final String SELECT_ALL_USERS =
            "select * " +
                    "from game.player";

    @Language("sql")
    private static final String SELECT_ALL_USERS_WHERE =
            "select * " +
                    "from game.player " +
                    "where ";

    @Language("sql")
    private static final String INSERT_USER_TEMPLATE =
            "insert into game.player (gameid, login) " +
                    "values ('%d', '%s');";

    @Language("sql")
    private static final String INSERT_INTO_TABLE_TEMPLATE =
            "insert into %s (login) " +
                    "values ('%s');";

    @Language("sql")
    private static final String DELETE_USER =
            "delete * " +
                    "from game.player " +
                    "where ";

    @Language("sql")
    private static final String RESET_SCHEMA = "drop schema if exists game cascade;\n" +
            "create schema game;";


    @Language("sql")
    private static final String RESET_TABLE = "DROP TABLE IF EXISTS game.player;\n" +
            "CREATE TABLE game.player (\n" +
            "  gameId   SERIAL          ,\n" +
            "  login    VARCHAR(20)     UNIQUE NOT NULL,\n" +
            "  PRIMARY KEY (login)\n" +
            ");";

    @Language("sql")
    private static final String CHECK_FOR_PLAYER =
            "SELECT * from serverdata.list where login = '%s'";


    @Language("sql")
    private static final String GET_PLAYER_RANK =
            "SELECT rank FROM serverdata.list where login = '%s'";


    @Override
    public List<Player> getAll() {
        List<Player> persons = new ArrayList<>();
        try (Connection con = DbConnector.getConnection();
             Statement stm = con.createStatement()
        ) {
            ResultSet rs = stm.executeQuery(SELECT_ALL_USERS);
            while (rs.next()) {
                persons.add(mapToPlayer(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to getAll.", e);
            return Collections.emptyList();
        }

        return persons;
    }

    @Override
    public List<Player> getAllWhere(String... conditions) {
        List<Player> persons = new ArrayList<>();
        try (Connection con = DbConnector.getConnection();
             Statement stm = con.createStatement()
        ) {

            String condition = String.join(" and ", conditions);
            ResultSet rs = stm.executeQuery(SELECT_ALL_USERS_WHERE + condition);
            while (rs.next()) {
                persons.add(mapToPlayer(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to getAll where.", e);
            return Collections.emptyList();
        }

        return persons;
    }

    @Override
    public void insert(Player player) {
        try (Connection con = DbConnector.getConnection();
             Statement stm = con.createStatement()
        ) {
            stm.execute(String.format(INSERT_USER_TEMPLATE, player.getGameId(), player.getLogin()));
        } catch (SQLException e) {
            log.error("Failed to create player {}", player.getLogin(), e);
        }
    }

    public void insertIntoTable(String table, String name) {
        try (Connection con = DbConnector.getConnection();
             Statement stm = con.createStatement()
        ) {
            if (!playerExists(name))
                stm.execute(String.format(INSERT_INTO_TABLE_TEMPLATE, table, name));
            else
                log.error("player already registered");
        } catch (SQLException e) {
            log.error("Failed to insert player " + name + " into table " + table, e);
        }
    }

    @Override
    public void delete(Player player) {
        try (Connection con = DbConnector.getConnection();
             Statement stm = con.createStatement()
        ) {
            stm.execute(String.format(DELETE_USER, player.getLogin()));
        } catch (SQLException e) {
            log.error("Failed to create player {}", player.getLogin(), e);
        }
    }

    public void reset() {
        try (Connection con = DbConnector.getConnection();
             Statement stm = con.createStatement()
        ) {
            stm.execute(String.format(RESET_SCHEMA));
            stm.execute(String.format(RESET_TABLE));
        } catch (SQLException e) {
            log.error("Failed to reset DB", e);
        }
    }

    public boolean playerExists(String name) {
        try (Connection con = DbConnector.getConnection();
            Statement stm = con.createStatement()) {
            ResultSet rs = stm.executeQuery(String.format(CHECK_FOR_PLAYER, name));
            int rscounter = 0;
            while (rs.next())
                rscounter++;
            return rscounter != 0;
        } catch (SQLException e) {
            log.error("Failed to check if player exists");
            return false;
        }
    }

    public int getPlayerRank(String name) {
        try (Connection con = DbConnector.getConnection();
        Statement stm = con.createStatement()) {
            ResultSet rs = stm.executeQuery(String.format(GET_PLAYER_RANK, name));
            rs.next();
            return rs.getInt("rank");
        } catch (SQLException e) {
            log.error("Failed to get player " + name + "rank");
            return 0;
        }
    }

    public Player getByName(String name) {
        throw new UnsupportedOperationException();
    }

    private static Player mapToPlayer(ResultSet rs) throws SQLException {
        return new Player()
                .setGameId(rs.getInt("gameId"))
                .setLogin(rs.getString("login"));
    }
}
