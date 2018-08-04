package system;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class MessageTest {

    private static final String src = "src", trg = "trg", msg = "msg";
    private static final Clock fixedClock = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"));

    private Message testMessage;

    @Before
    public void setUp() {
        testMessage = new Message(src, trg, msg, fixedClock);
    }

    @After
    public void tearDown() {
        testMessage = null;
    }

    @Test
    public void getSourceAgentId_returnsSourceAgentId() {
        Assert.assertEquals(src, testMessage.getSourceAgentId());
    }

    @Test
    public void getTargetAgentId_returnsTargetAgentId() {
        Assert.assertEquals(trg, testMessage.getTargetAgentId());
    }

    @Test
    public void getTimestamp_returnsTimestamp() {
        Assert.assertEquals(Instant.now(fixedClock), testMessage.getTimestamp());
    }

    @Test
    public void getMessage_returnsMessage() {
        Assert.assertEquals(msg, testMessage.getMessage());
    }
}
