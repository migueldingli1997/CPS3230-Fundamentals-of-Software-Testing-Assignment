package system;

import util.TemporaryObject;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

class TemporaryKey extends TemporaryObject<String> {

    TemporaryKey(String key, Duration timeLimit) {
        super(key, Instant.now().plus(timeLimit), Clock.systemUTC());
    }

    public String getKey() {
        return getTempObject();
    }

    /**
     * Checks that the key has not expired and compares the
     * stored temporary string to the argument key.
     *
     * @param anotherKey String to compare the stored string to.
     * @return True if key is not expired and strings match.
     */
    public boolean equals(String anotherKey) {
        return !this.isExpired() && getTempObject().equals(anotherKey);
    }
}
