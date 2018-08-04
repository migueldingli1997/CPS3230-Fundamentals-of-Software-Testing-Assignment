package system;

import util.TemporaryObject;

import java.time.Clock;
import java.time.Instant;

public class Message extends TemporaryObject<String> {

    private final String sourceAgentId; // sender
    private final String targetAgentId; // receiver

    /**
     * Creates a new message
     *
     * @param sourceAgentId Sender of the message
     * @param targetAgentId Receiver of the message
     * @param message       The message contents
     */
    Message(String sourceAgentId, String targetAgentId, String message) {
        this(sourceAgentId, targetAgentId, message, Clock.systemUTC());
    }

    /**
     * Creates a new message
     *
     * @param sourceAgentId Sender of the message
     * @param targetAgentId Receiver of the message
     * @param message       The message contents
     * @param clock         Clock to use for setting the timestamp
     */
    Message(String sourceAgentId, String targetAgentId, String message, Clock clock) {
        super(message, Instant.now(clock).plus(Mailbox.MESSAGE_TIME_LIMIT), clock);
        this.sourceAgentId = sourceAgentId;
        this.targetAgentId = targetAgentId;
    }

    public String getSourceAgentId() {
        return sourceAgentId;
    }

    public String getTargetAgentId() {
        return targetAgentId;
    }

    public Instant getTimestamp() {
        return getTimeout().minus(Mailbox.MESSAGE_TIME_LIMIT); // only used for displaying the timestamp
    }

    public String getMessage() {
        return getTempObject();
    }
}
