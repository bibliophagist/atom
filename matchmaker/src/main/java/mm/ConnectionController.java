package mm;

import mm.dao.PlayerDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("matchmaker")
public class ConnectionController {
    private static final Logger log = LogManager.getLogger(ConnectionController.class);

    @Autowired
    static Matchmaker matchMaker = new Matchmaker();
    private static PlayerDao playerDao = new PlayerDao();

    private static HttpHeaders headers = new HttpHeaders();

    static {
        headers.add("Access-Control-Allow-Origin", "*");
        Thread matchMakerThread = new Thread(matchMaker);
        matchMakerThread.start();
        playerDao.reset();
    }

    /**
     * curl test
     * <p>
     * curl -i -X POST -H "Content-Type: application/x-www-form-urlencoded" localhost:8080/matchmaker/join -d "name=bomberman"
     */

    @RequestMapping(
            path = "join",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> join(@RequestParam("name") String name) {
        if (!playerDao.playerExists(name)) {
            //TODO: change HttpStatus
            return new ResponseEntity<>("no player with such name, use register instead", headers, HttpStatus.BAD_REQUEST);
        }
        log.info(name + " joins");
        if (matchMaker.join(name)) {
            log.info(name + " added to queue");
        } else {
            log.error(name + " cannot be added to queue");
            //TODO: return error response entity
        }
        Long gameId = null;
        while (gameId == null) {
            gameId = matchMaker.inGamePlayers.get(name);
            if (gameId == null)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
        }
        log.info("gameId = " + gameId.toString() + " sent to " + name);
        return new ResponseEntity<>(gameId.toString(), headers, HttpStatus.OK);
    }

    /**
     * curl test
     * <p>
     * curl -i -X POST -H "Content-Type: application/x-www-form-urlencoded" localhost:8080/matchmaker/register -d "name=test"
     *</p>
     */

    @RequestMapping(
            path = "register",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> register(@RequestParam("name") String name) {
        playerDao.insertIntoTable("playerlist.list", name);
        return new ResponseEntity<>(name + " registered", headers, HttpStatus.OK);
    }

    /**
     * curl test
     * <p>
     * curl -i localhost:8080/matchmaker/list'
     */
    @RequestMapping(
            path = "list",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> list() {
        log.info("Games list request");
        String openingTag = "<html><body><table>";
        String tableHead = "<tr> <th>%s</th>><tr> ";
        String tableRow = "<tr> <td> %s </td> <td> %s </td> <td>%s</td> </tr>";
        String closingTag = "</table></body></html>";
        return new ResponseEntity<>(tableHead, HttpStatus.OK);
    }

}
