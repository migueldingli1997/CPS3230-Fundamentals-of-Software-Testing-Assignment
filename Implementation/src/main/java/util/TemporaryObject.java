package util;

import java.time.Clock;
import java.time.Instant;

/**
 * An object that would timeout must extend this class.
 * It provides functionality for timing objects out
 */
public abstract class TemporaryObject<T> {

    private final Instant timeout;
    private final Clock clock;
    private T tempObject;

    protected TemporaryObject(T tempObject, Instant timeout, Clock clock) {
        this.tempObject = tempObject;
        this.timeout = timeout;
        this.clock = clock;
    }

    public boolean isExpired() {
        final Instant now = Instant.now(clock);
        return now.equals(timeout) || now.isAfter(timeout);
    }

    protected Instant getTimeout() {
        return timeout;
    }

    protected T getTempObject() {
        tempObject = isExpired() ? null : tempObject;
        return tempObject;
    }
}
