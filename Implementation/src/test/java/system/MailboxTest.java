package system;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MailboxTest {

    private static final String OWNER_ID = "1234xy", SENDER_ID = "5678vw", MESSAGE = "message";

    private Mailbox testEmptyMailbox;
    private Mailbox testMailboxWith1Message;
    private Queue<Message> messageQueue;

    @Mock
    private Message mockMessage;

    @Before
    public void setUp() {

        testEmptyMailbox = new Mailbox(OWNER_ID);

        messageQueue = new LinkedBlockingQueue<>();
        testMailboxWith1Message = new Mailbox(OWNER_ID, messageQueue);
        messageQueue.add(mockMessage);

        when(mockMessage.getSourceAgentId()).thenReturn(SENDER_ID);
        when(mockMessage.getTargetAgentId()).thenReturn(OWNER_ID);
        when(mockMessage.getMessage()).thenReturn(MESSAGE);
        when(mockMessage.isExpired()).thenReturn(false);
    }

    @After
    public void tearDown() {
        testEmptyMailbox = null;
        testMailboxWith1Message = null;
        messageQueue = null;
    }

    @Test
    public void consumeNextMessage_notNullIfMailboxHasMessage() {
        Assert.assertNotNull(testMailboxWith1Message.consumeNextMessage());
    }

    @Test
    public void consumeNextMessage_nullIfMailboxIsEmpty() {
        Assert.assertNull(testEmptyMailbox.consumeNextMessage());
    }

    @Test
    public void consumeNextMessage_unsuccessfulIfTimeLimitExceeded() {
        when(mockMessage.isExpired()).thenReturn(true);

        Assert.assertNull(testMailboxWith1Message.consumeNextMessage());
    }

    @Test
    public void hasMessages_trueIfMailboxHasMessages() {
        Assert.assertTrue(testMailboxWith1Message.hasMessages());
    }

    @Test
    public void hasMessages_falseIfMailboxIsEmpty() {
        Assert.assertFalse(testEmptyMailbox.hasMessages());
    }

    @Test
    public void hasMessages_falseIfTimeLimitExceeded() {
        when(mockMessage.isExpired()).thenReturn(true);

        Assert.assertFalse(testMailboxWith1Message.hasMessages());
    }

    @Test
    public void addMessage_trueIfSuccessful() {
        Assert.assertTrue(testEmptyMailbox.addMessage(mockMessage));
    }

    @Test
    public void addMessage_falseIfMessageTimestampIsTooLongAgo() {
        when(mockMessage.isExpired()).thenReturn(true);

        Assert.assertFalse(testEmptyMailbox.addMessage(mockMessage));
    }

    @Test
    public void addMessage_falseIfOwnerIsNotMessageTarget() {
        when(mockMessage.getTargetAgentId()).thenReturn("AnotherID");

        Assert.assertFalse(testEmptyMailbox.addMessage(mockMessage));
    }
}