package system;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SupervisorImplTest {

    // Agent details
    private static final String VALID_AGENT_ID = "1234xy";
    private static final String INVALID_AGENT_ID = "spy-" + VALID_AGENT_ID;

    private SupervisorImpl testSupervisor;

    @Mock
    private MessagingSystem mockMessagingSystem;

    @Before
    public void setUp() {
        testSupervisor = new SupervisorImpl(mockMessagingSystem);
    }

    @After
    public void tearDown() {
        mockMessagingSystem = null;
        testSupervisor = null;
    }

    @Test
    public void getLoginKey_loginKeyIfAgentIdValid() {
        when(mockMessagingSystem.registerLoginKey(Mockito.eq(VALID_AGENT_ID), Mockito.anyString())).thenReturn(true);

        Assert.assertNotEquals(null, testSupervisor.getLoginKey(VALID_AGENT_ID));
    }

    @Test
    public void getLoginKey_nullIfAgentIdStartsWithSpy() {
        Assert.assertEquals(null, testSupervisor.getLoginKey(INVALID_AGENT_ID));
    }
}
