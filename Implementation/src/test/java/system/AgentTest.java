package system;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import util.Utils;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AgentTest {

    // Main agent details
    private static final String AGENT_ID = "1234xy";
    private static final String LOGIN_KEY = Utils.getNCharacters(10);
    private static final String SESSN_KEY = Utils.getNCharacters(50);

    // For messaging
    private static final String TARGET_AGENT_ID = "5678ab";
    private static final String MESSAGE = "message";

    private Agent testAgent_default;
    private Agent testAgent_registered;
    private Agent testAgent_loggedIn;

    @Mock
    private Supervisor mockSupervisor;
    @Mock
    private MessagingSystem mockMessagingSystem;

    @Before
    public void setUp() {
        testAgent_default = new Agent(AGENT_ID, mockSupervisor, mockMessagingSystem);
        testAgent_registered = new Agent(AGENT_ID, mockSupervisor, mockMessagingSystem, LOGIN_KEY, null);
        testAgent_loggedIn = new Agent(AGENT_ID, mockSupervisor, mockMessagingSystem, LOGIN_KEY, SESSN_KEY);
    }

    @After
    public void tearDown() {
        mockSupervisor = null;
        mockMessagingSystem = null;
        testAgent_default = null;
        testAgent_registered = null;
        testAgent_loggedIn = null;
    }

    @Test
    public void register_trueIfSuccessful() {
        when(mockSupervisor.getLoginKey(AGENT_ID)).thenReturn(LOGIN_KEY);

        Assert.assertTrue(testAgent_default.register());
    }

    @Test
    public void register_falseIfNoLoginKeyFromSupervisor() {
        when(mockSupervisor.getLoginKey(Mockito.anyString())).thenReturn(null);

        Assert.assertFalse(testAgent_default.register());
    }

    @Test
    public void login_trueIfSuccessful() {
        when(mockMessagingSystem.login(AGENT_ID, LOGIN_KEY)).thenReturn(Utils.getNCharacters(50));

        Assert.assertTrue(testAgent_registered.login());
    }

    @Test
    public void login_falseIfNoLoginKeyFromSupervisor() {
        when(mockMessagingSystem.login(AGENT_ID, LOGIN_KEY)).thenReturn(null);

        Assert.assertFalse(testAgent_registered.login());
    }

    @Test
    public void sendMessage_falseIfNotLoggedIn() {
        // by default agent is not logged in

        Assert.assertEquals(StatusCodes.SOURCE_AGENT_NOT_LOGGED_IN, testAgent_default.sendMessage(TARGET_AGENT_ID, MESSAGE));
    }

    @Test
    public void sendMessage_falseIfMessagingSystemFailure() {
        when(mockMessagingSystem.sendMessage(Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString())).thenReturn(StatusCodes.GENERIC_ERROR);

        Assert.assertEquals(StatusCodes.GENERIC_ERROR, testAgent_loggedIn.sendMessage(TARGET_AGENT_ID, MESSAGE));
    }

    @Test
    public void sendMessage_trueIfSuccessful() {
        when(mockMessagingSystem.sendMessage(Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString())).thenReturn(StatusCodes.OK);

        Assert.assertEquals(StatusCodes.OK, testAgent_loggedIn.sendMessage(TARGET_AGENT_ID, MESSAGE));
    }

    @Test
    public void getLoginKey_returnsLoginKey() {
        Assert.assertEquals(LOGIN_KEY, testAgent_loggedIn.getLoginKey());
    }

    @Test
    public void getSessionKey_returnsSessionKey() {
        Assert.assertEquals(SESSN_KEY, testAgent_loggedIn.getSessionKey());
    }
}