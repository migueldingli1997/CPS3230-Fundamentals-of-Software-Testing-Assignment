package util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class TemporaryObjectTest {

    private final String TEMP_STRING = "Temporary String";
    private final Duration TIMEOUT = Duration.ofMinutes(5);

    private final Instant START_TIME = Instant.EPOCH;

    // Starts a 5 minute step clock 1 second after EPOCH
    private final Clock TEST_STEP_CLOCK = new StepClock(START_TIME.plusSeconds(1), TIMEOUT);

    private TemporaryObject<String> testTempObject;

    @Before
    public void setUp() {
        testTempObject = new TemporaryObject<String>(TEMP_STRING, START_TIME.plus(TIMEOUT), TEST_STEP_CLOCK) {
        };
    }

    @After
    public void tearDown() {
        testTempObject = null;
    }

    @Test
    public void isExpired_falseTimeLimitNotPassed() {
        Assert.assertFalse(testTempObject.isExpired());
    }

    @Test
    public void isExpired_trueTimeLimitPassed() {
        Instant.now(TEST_STEP_CLOCK); // First call returns 1 second after Instant.EPOCH

        Assert.assertTrue(testTempObject.isExpired());
    }

    @Test
    public void getTimeout_shouldReturnTimeout() {
        Assert.assertEquals(Instant.EPOCH.plus(TIMEOUT), testTempObject.getTimeout());
    }

    @Test
    public void getTempObject_beforeTimeoutShouldReturnTempObject() {
        Assert.assertEquals(TEMP_STRING, testTempObject.getTempObject());
    }

    @Test
    public void getTempObject_afterTimeoutShouldReturnNull() {
        Instant.now(TEST_STEP_CLOCK); // First call returns 1 second after Instant.EPOCH

        Assert.assertNull(testTempObject.getTempObject());
    }

    private class StepClock extends Clock {

        private final Instant baseTime;
        private final Duration step;
        private int stepMultiplier = 0;

        StepClock(Instant baseTime, Duration step) {
            this.baseTime = baseTime;
            this.step = step;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        //Ignored
        public Clock withZone(ZoneId zoneId) {
            return this;
        }

        @Override
        public Instant instant() {
            return baseTime.plus(step.multipliedBy(stepMultiplier++));
        }
    }
}
