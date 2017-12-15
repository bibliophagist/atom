package gs.util;

import gs.geometry.Point;
import gs.message.Message;
import gs.message.Topic;
import gs.model.Wall;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonHelperTest {
    @Test
    public void toJson() throws Exception {
        Point point = new Point(2, 3);
        assertEquals("{\"x\":2,\"y\":3}", JsonHelper.toJson(point));

        Wall wall = new Wall(5, 10, Wall.Type.Wood);
        assertEquals("{\"id\":0,\"point\":{\"x\":5,\"y\":10},\"type\":\"Wood\"}", JsonHelper.toJson(wall));

        Message msg = new Message(Topic.PLANT_BOMB, "12.12.12");
        msg.setOwner("alice");
        assertEquals("{\"topic\":\"PLANT_BOMB\",\"data\":\"12.12.12\",\"owner\":\"alice\"}", JsonHelper.toJson(msg));
    }

    @Test
    public void fromJson() throws Exception {
        Message msg = new Message(Topic.HELLO, "\"12.12.12\"");
        msg.setOwner("alice");
        String jmsg = "{\"topic\":\"HELLO\",\"data\":\"12.12.12\",\"owner\":\"alice\"}";
        assertEquals(msg, JsonHelper.fromJson(jmsg, Message.class));
    }
}
