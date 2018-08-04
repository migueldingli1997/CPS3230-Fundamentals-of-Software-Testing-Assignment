package system;

import util.TemporaryObject;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class encapsulates the functionality of a mailbox that holds all container for a user.
 */
class Mailbox {

    static final Duration MESSAGE_TIME_LIMIT = Duration.ofMinutes(30);

    private final Queue<Message> messages;
    private final String ownerId;

    Mailbox(String ownerId) {
        this(ownerId, new LinkedBlockingQueue<>());
    }

    Mailbox(String ownerId, Queue<Message> messages) {
        this.ownerId = ownerId;
        this.messages = messages;
    }

    /**
     * Returns the next message in the box on a FIFO basis.
     *
     * @return A message or null if the mailbox is empty.
     */
    public Message consumeNextMessage() {
        messages.removeIf(TemporaryObject::isExpired);
        return messages.poll();
    }

    /**
     * Checks if there are any container in the mailbox.
     *
     * @return true if there is at least one message in the mailbox.
     */
    public boolean hasMessages() {
        messages.removeIf(TemporaryObject::isExpired);
        return !messages.isEmpty();
    }

    /**
     * Adds a message to the mailbox.
     *
     * @param message Message to add to mailbox.
     * @return true if successful, false otherwise.
     */
    public boolean addMessage(Message message) {
        messages.removeIf(TemporaryObject::isExpired);
        return message.getTargetAgentId().equals(this.ownerId)
                && !message.isExpired()
                && messages.offer(message);
    }
}
