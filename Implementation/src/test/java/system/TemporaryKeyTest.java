package system;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

public class TemporaryKeyTest {

    private static final String keyString = "temporaryKey";

    private TemporaryKey testNonExpiredKey;
    private TemporaryKey testExpiredKey;

    @Before
    public void setUp() {
        testNonExpiredKey = new TemporaryKey(keyString, Duration.ofSeconds(1));
        testExpiredKey = new TemporaryKey(keyString, Duration.ZERO);
    }

    @After
    public void tearDown() {
        testNonExpiredKey = null;
        testExpiredKey = null;
    }

    @Test
    public void getKey_nullIfKeyExpired() {
        Assert.assertNull(testExpiredKey.getKey());
    }

    @Test
    public void getKey_returnsKeyIfNotExpired() {
        Assert.assertEquals(keyString, testNonExpiredKey.getKey());
    }

    @Test
    public void equals_falseIfKeysUnequal() {
        Assert.assertFalse(testNonExpiredKey.equals("anotherKey"));
    }

    @Test
    public void equals_falseIfKeyExpired() {
        Assert.assertFalse(testExpiredKey.equals(keyString));
    }

    @Test
    public void equals_trueIfKeysEqualAndNotExpired() {
        Assert.assertTrue(testNonExpiredKey.equals(keyString));
    }
}
