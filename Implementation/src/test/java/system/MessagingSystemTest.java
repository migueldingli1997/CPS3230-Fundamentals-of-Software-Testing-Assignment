package system;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import util.Utils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MessagingSystemTest {

    private static final int LOGIN_KEY_LENGTH = 10;
    private static final int SESSION_KEY_LENGTH = 50;
    private static final int MAX_MESSAGE_LENGTH = 140;
    private static final int MAX_MESSAGES_SENT = 25;
    private static final int MAX_MESSAGES_RECV = 25;

    // Valid login keys (one per agent)
    private static final String VALID_LKEY_1 = Utils.getNCharacters(LOGIN_KEY_LENGTH, "1");
    private static final String VALID_LKEY_2 = Utils.getNCharacters(LOGIN_KEY_LENGTH, "2");

    // Valid session keys (one per agent)
    private static final String VALID_SKEY_1 = Utils.getNCharacters(SESSION_KEY_LENGTH, "1");
    private static final String VALID_SKEY_2 = Utils.getNCharacters(SESSION_KEY_LENGTH, "2");

    // Two agent IDs and valid message
    private static final String AID_1 = "1234xy";
    private static final String AID_2 = "5678ab";
    private static final String VALID_MSG = "msg";

    @Mock
    private TemporaryKey mockLoginKey1;
    @Mock
    private TemporaryKey mockLoginKey2;
    @Mock
    private TemporaryKey mockSessnKey1;
    @Mock
    private TemporaryKey mockSessnKey2;

    private MessagingSystem testSystem;
    private Map<String, AgentInfo> agentInfos;

    @Before
    public void setUp() {
        agentInfos = new HashMap<>();
        testSystem = new MessagingSystem(agentInfos);

        when(mockLoginKey1.getKey()).thenReturn(VALID_LKEY_1);
        when(mockSessnKey1.getKey()).thenReturn(VALID_SKEY_1);
        when(mockLoginKey2.getKey()).thenReturn(VALID_LKEY_2);
        when(mockSessnKey2.getKey()).thenReturn(VALID_SKEY_2);

        when(mockLoginKey1.isExpired()).thenReturn(false);
        when(mockSessnKey1.isExpired()).thenReturn(false);
        when(mockLoginKey2.isExpired()).thenReturn(false);
        when(mockSessnKey2.isExpired()).thenReturn(false);

        when(mockLoginKey1.equals(VALID_LKEY_1)).thenReturn(true);
        when(mockSessnKey1.equals(VALID_SKEY_1)).thenReturn(true);
        when(mockLoginKey2.equals(VALID_LKEY_2)).thenReturn(true);
        when(mockSessnKey2.equals(VALID_SKEY_2)).thenReturn(true);
    }

    @After
    public void tearDown() {
        agentInfos = null;
        testSystem = null;
    }

    @Test
    public void register_falseIfLoginKeyIncorrectLength() {
        Assert.assertFalse(testSystem.registerLoginKey(AID_1, Utils.getNCharacters(LOGIN_KEY_LENGTH + 1)));
        Assert.assertFalse(testSystem.registerLoginKey(AID_1, Utils.getNCharacters(LOGIN_KEY_LENGTH - 1)));
    }

    @Test
    public void register_falseIfLoginKeyNotUnique() {
        addAgent(agentInfos, 1, AddType.REGISTERED);

        Assert.assertFalse(testSystem.registerLoginKey(AID_2, VALID_LKEY_1));
    }

    @Test
    public void register_trueIfLoginKeyValid() {
        Assert.assertTrue(testSystem.registerLoginKey(AID_1, VALID_LKEY_1));
    }

    @Test
    public void login_nullIfAgentDoesNotExist() {
        Assert.assertNull(testSystem.login(AID_1, VALID_LKEY_1));
    }

    @Test
    public void login_nullIfAgentDidNotRegister() {
        addAgent(agentInfos, 1, AddType.UNREGISTERED);

        Assert.assertNull(testSystem.login(AID_1, VALID_LKEY_1));
    }

    @Test
    public void login_nullIfAgentExistsButDidNotRegister() {
        addAgent(agentInfos, 1, AddType.UNREGISTERED);

        Assert.assertNull(testSystem.login(AID_1, VALID_LKEY_1)); // given a valid login key before registering
        Assert.assertNull(testSystem.login(AID_1, null)); // when agent.login() called before registering
    }

    @Test
    public void login_nullIfLoginKeyExpired() {
        when(mockLoginKey1.getKey()).thenReturn(null);
        when(mockLoginKey1.isExpired()).thenReturn(true);
        when(mockLoginKey1.equals(VALID_LKEY_1)).thenReturn(false);

        addAgent(agentInfos, 1, AddType.REGISTERED);

        Assert.assertNull(testSystem.login(AID_1, VALID_LKEY_1));
    }

    @Test
    public void login_nullIfLoginKeyDoesNotMatch() {
        addAgent(agentInfos, 1, AddType.REGISTERED);

        Assert.assertNull(testSystem.login(AID_1, VALID_LKEY_2));
    }

    @Test
    public void login_validSessionKeyIfLoginKeyValid() {
        addAgent(agentInfos, 1, AddType.REGISTERED);

        final String sessionKey = testSystem.login(AID_1, VALID_LKEY_1);
        Assert.assertNotNull(sessionKey);
        Assert.assertTrue(sessionKey.length() == SESSION_KEY_LENGTH);
    }

    @Test
    public void logout_falseIfAgentDoesNotExist() {
        Assert.assertFalse(testSystem.logout(AID_1));
    }

    @Test
    public void logout_trueIfAgentExistsButNotLoggedIn() {
        addAgent(agentInfos, 1, AddType.REGISTERED);

        Assert.assertTrue(testSystem.logout(AID_1));
    }

    @Test
    public void logout_trueIfAgentHadSentOrReceivedButCountersReset() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);

        AgentInfo agentInfo = agentInfos.get(AID_1);
        agentInfo.messagesSent = 5;
        agentInfo.messagesRecv = 5;

        Assume.assumeTrue(testSystem.logout(AID_1));
        Assert.assertEquals(0, agentInfo.messagesSent);
        Assert.assertEquals(0, agentInfo.messagesRecv);
    }

    @Test
    public void logout_trueIfAgentLoggedIn() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);

        Assert.assertTrue(testSystem.logout(AID_1));
    }

    @Test
    public void logout_setsAgentSessionKeyToExpiredKey() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);

        Assume.assumeTrue(testSystem.logout(AID_1));
        Assert.assertTrue(agentInfos.get(AID_1).sessionKey.isExpired());
    }

    @Test
    public void sendMessage_failsIfSourceAgentDoesNotExist() {
        addAgent(agentInfos, 2, AddType.REGISTERED); // only target agent exists

        Assert.assertEquals(StatusCodes.SOURCE_AGENT_DOES_NOT_EXIST, testSystem.sendMessage(VALID_SKEY_1, AID_1, AID_2, VALID_MSG));
    }

    @Test
    public void sendMessage_failsIfTargetAgentDoesNotExist() {
        addAgent(agentInfos, 1, AddType.REGISTERED); // only source agent exists

        Assert.assertEquals(StatusCodes.TARGET_AGENT_DOES_NOT_EXIST, testSystem.sendMessage(VALID_SKEY_1, AID_1, AID_2, VALID_MSG));
    }

    @Test
    public void sendMessage_failsIfSourceAgentDidNotLogin() {
        addAgent(agentInfos, 1, AddType.REGISTERED); // source did not login
        addAgent(agentInfos, 2, AddType.REGISTERED); // target doesn't have to be logged in

        Assert.assertEquals(StatusCodes.SOURCE_AGENT_NOT_LOGGED_IN, testSystem.sendMessage(VALID_SKEY_1, AID_1, AID_2, VALID_MSG));
    }

    @Test
    public void sendMessage_failsIfSessionKeyExpired() {
        when(mockSessnKey1.getKey()).thenReturn(null);
        when(mockSessnKey1.isExpired()).thenReturn(true);
        when(mockSessnKey1.equals(VALID_SKEY_1)).thenReturn(false);

        addAgent(agentInfos, 1, AddType.LOGGEDIN);   // source must be logged in
        addAgent(agentInfos, 2, AddType.REGISTERED); // target doesn't have to be logged in

        Assert.assertEquals(StatusCodes.SOURCE_AGENT_NOT_LOGGED_IN, testSystem.sendMessage(VALID_SKEY_1, AID_1, AID_2, VALID_MSG));
    }

    @Test
    public void sendMessage_failsIfSessionKeyDoesNotMatch() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);   // source must be logged in
        addAgent(agentInfos, 2, AddType.REGISTERED); // target doesn't have to be logged in

        Assert.assertEquals(StatusCodes.SESSION_KEY_UNRECOGNIZED, testSystem.sendMessage(VALID_SKEY_2, AID_1, AID_2, VALID_MSG));
    }

    @Test
    public void sendMessage_failsIfMessageLengthExceeded() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);   // source must be logged in
        addAgent(agentInfos, 2, AddType.REGISTERED); // target doesn't have to be logged in

        final String LONG_MESSAGE = Utils.getNCharacters(MAX_MESSAGE_LENGTH + 1);
        Assert.assertEquals(StatusCodes.MESSAGE_LENGTH_EXCEEDED, testSystem.sendMessage(VALID_SKEY_1, AID_1, AID_2, LONG_MESSAGE));
    }

    @Test
    public void sendMessage_okIfContainsBlockedWordsButTheyAreRemoved() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);   // source must be logged in
        addAgent(agentInfos, 2, AddType.REGISTERED); // target doesn't have to be logged in

        final String wordsThatShouldBeBlocked[] = {"recipe", "ginger", "nuclear"};

        for (String bw : wordsThatShouldBeBlocked) {

            final String altCapBW = alternateCapitalization(bw);
            final String message_sent = VALID_MSG + altCapBW + " " + altCapBW + VALID_MSG;
            final String expect_to_receive = message_sent.replaceAll(altCapBW + "\\s?", "");

            Assume.assumeTrue(StatusCodes.OK == testSystem.sendMessage(VALID_SKEY_1, AID_1, AID_2, message_sent));
            Assert.assertEquals(expect_to_receive, agentInfos.get(AID_2).mailbox.consumeNextMessage().getMessage());
        }
    }

    @Test
    public void sendMessage_sourceAgentGetsLoggedOutIfQuotaReached() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);   // source must be logged in
        addAgent(agentInfos, 2, AddType.REGISTERED); // target doesn't have to be logged in

        agentInfos.get(AID_1).messagesSent = MAX_MESSAGES_SENT;

        Assume.assumeTrue(StatusCodes.SOURCE_AGENT_QUOTA_EXCEEDED ==
                testSystem.sendMessage(VALID_SKEY_1, AID_1, AID_2, VALID_MSG));
        Assert.assertTrue(agentInfos.get(AID_1).sessionKey.isExpired());
    }

    @Test
    public void sendMessage_targetAgentGetsLoggedOutIfQuotaReached() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN); // source must be logged in
        addAgent(agentInfos, 2, AddType.LOGGEDIN); // target is logged in so that final assert makes sense

        agentInfos.get(AID_2).messagesRecv = MAX_MESSAGES_RECV;

        Assume.assumeTrue(StatusCodes.TARGET_AGENT_QUOTA_EXCEEDED ==
                testSystem.sendMessage(VALID_SKEY_1, AID_1, AID_2, VALID_MSG));
        Assert.assertTrue(agentInfos.get(AID_2).sessionKey.isExpired());
    }

    @Test
    public void sendMessage_bothAgentsLoggedOutIfQuotasReached() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN); // source must be logged in
        addAgent(agentInfos, 2, AddType.LOGGEDIN); // target is logged in so that final assert makes sense

        agentInfos.get(AID_1).messagesSent = MAX_MESSAGES_SENT;
        agentInfos.get(AID_2).messagesRecv = MAX_MESSAGES_RECV;

        Assume.assumeTrue(StatusCodes.BOTH_AGENT_QUOTAS_EXCEEDED ==
                testSystem.sendMessage(VALID_SKEY_1, AID_1, AID_2, VALID_MSG));
        Assert.assertTrue(agentInfos.get(AID_1).sessionKey.isExpired());
        Assert.assertTrue(agentInfos.get(AID_2).sessionKey.isExpired());
    }

    @Test
    public void sendMessage_okIfAllValid() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);   // source must be logged in
        addAgent(agentInfos, 2, AddType.REGISTERED); // target doesn't have to be logged in

        Assert.assertEquals(StatusCodes.OK, testSystem.sendMessage(VALID_SKEY_1, AID_1, AID_2, VALID_MSG));
    }

    @Test
    public void agentHasMessages_falseIfAgentDoesNotExist() {
        Assert.assertFalse(testSystem.agentHasMessages(AID_1));
    }

    @Test
    public void agentHasMessages_falseIfAgentNotLoggedIn() {
        addAgent(agentInfos, 1, AddType.REGISTERED);

        Assert.assertFalse(testSystem.agentHasMessages(AID_1));
    }

    @Test
    public void agentHasMessages_falseIfSessionKeyDoesNotMatch() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);

        Assert.assertFalse(testSystem.agentHasMessages(AID_1));
    }

    @Test
    public void agentHasMessages_falseIfAgentDoesNotHaveMessages() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);

        Assert.assertFalse(testSystem.agentHasMessages(AID_1));
    }

    @Test
    public void agentHasMessages_trueIfAgentHasMessages() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);
        agentInfos.get(AID_1).mailbox.addMessage(new Message(AID_2, AID_1, "msg"));

        Assert.assertTrue(testSystem.agentHasMessages(AID_1));
    }

    @Test
    public void getNextMessage_nullIfAgentDoesNotExist() {
        Assert.assertNull(testSystem.getNextMessage(VALID_SKEY_1, AID_1));
    }

    @Test
    public void getNextMessage_nullIfAgentNotLoggedIn() {
        addAgent(agentInfos, 1, AddType.REGISTERED);

        Assert.assertNull(testSystem.getNextMessage(VALID_LKEY_1, AID_1));
    }

    @Test
    public void getNextMessage_nullIfSessionKeyDoesNotMatch() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);

        Assert.assertNull(testSystem.getNextMessage(VALID_SKEY_2, AID_1));
    }

    @Test
    public void getNextMessage_nullIfAgentDoesNotHaveMessages() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);

        Assert.assertNull(testSystem.getNextMessage(VALID_SKEY_1, AID_1));
    }

    @Test
    public void getNextMessage_returnsFirstMessageInTheMailboxIfAgentHasMessages() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);

        final String message1 = "msg1";
        final String message2 = "msg2";

        agentInfos.get(AID_1).mailbox.addMessage(new Message(AID_2, AID_1, message1));
        agentInfos.get(AID_1).mailbox.addMessage(new Message(AID_2, AID_1, message2));

        Assert.assertEquals(message1, testSystem.getNextMessage(VALID_SKEY_1, AID_1).getMessage());
    }

    @Test
    public void agentLoggedIn_returnsFalseIfAgentDoesNotExist() {
        Assert.assertFalse(testSystem.agentLoggedIn(AID_1));
    }

    @Test
    public void agentLoggedIn_returnsFalseIfAgentNotLoggedIn() {
        addAgent(agentInfos, 1, AddType.REGISTERED);

        Assert.assertFalse(testSystem.agentLoggedIn(AID_1));
    }

    @Test
    public void agentLoggedIn_returnsTrueIfAgentLoggedIn() {
        addAgent(agentInfos, 1, AddType.LOGGEDIN);

        Assert.assertTrue(testSystem.agentLoggedIn(AID_1));
    }

    private void addAgent(final Map<String, AgentInfo> agentInfos, int agent, AddType type) {

        Assume.assumeTrue(agent == 1 || agent == 2);

        final String agentId = (agent == 1 ? AID_1 : AID_2);
        final TemporaryKey loginKey = (agent == 1 ? mockLoginKey1 : mockLoginKey2);
        final TemporaryKey sessnKey = (agent == 1 ? mockSessnKey1 : mockSessnKey2);
        final AgentInfo agentInfo = new AgentInfo(agentId);

        switch (type) {
            case REGISTERED:
                agentInfo.loginKey = loginKey;
                break;
            case LOGGEDIN:
                agentInfo.sessionKey = sessnKey;
                break;
            default:
                break;
        }
        // Note: if AddType.UNREGISTERED, nothing is done

        agentInfos.put(agentId, agentInfo);
    }

    private String alternateCapitalization(String string) {
        StringBuilder stringBuilder = new StringBuilder();

        boolean alternator = true;
        for (char ch : string.toCharArray()) {
            stringBuilder.append(alternator ? Character.toUpperCase(ch) : Character.toLowerCase(ch));
            alternator = !alternator;
        }

        return stringBuilder.toString();
    }

    private enum AddType {
        UNREGISTERED,
        REGISTERED,
        LOGGEDIN
    }
}