package system;

/**
 * This class encapsulates an Agent that can use the system.
 */
public class Agent {

    private final String id;
    private final Supervisor supervisor;
    private final MessagingSystem messagingSystem;

    private String loginKey = null;
    private String sessionKey = null;

    public Agent(String id, Supervisor supervisor, MessagingSystem messagingSystem) {
        this.id = id;
        this.supervisor = supervisor;
        this.messagingSystem = messagingSystem;
    }

    public Agent(String id, Supervisor supervisor, MessagingSystem messagingSystem, String loginKey) {
        this(id, supervisor, messagingSystem);
        this.loginKey = loginKey;
    }

    Agent(String id, Supervisor supervisor, MessagingSystem messagingSystem, String loginKey, String sessionKey) {
        this(id, supervisor, messagingSystem, loginKey);
        this.sessionKey = sessionKey;
    }

    /**
     * Initiates contact with the supervisor to get a login key
     *
     * @return true if login key successfully obtained, false otherwise
     */
    public boolean register() {
        loginKey = supervisor.getLoginKey(id);
        return loginKey != null;
    }

    /**
     * Logs into the system using the previously obtained login key
     *
     * @return true if login successful, false otherwise.
     */
    public boolean login() {
        sessionKey = messagingSystem.login(id, loginKey);
        return sessionKey != null;
    }

    /**
     * Sends a message to the destination agent.
     *
     * @param destinationAgentId The id of the destination agent.
     * @param message            The content of the message.
     * @return true if successful, false otherwise.
     */
    public StatusCodes sendMessage(final String destinationAgentId, final String message) {

        if (sessionKey == null) {
            return StatusCodes.SOURCE_AGENT_NOT_LOGGED_IN;
        } else {
            return messagingSystem.sendMessage(sessionKey, id, destinationAgentId, message);
        }
    }

    /**
     * @return the login key that the agent has
     */
    public String getLoginKey() {
        return loginKey;
    }

    /**
     * @return the session key that the agent has
     */
    public String getSessionKey() {
        return sessionKey;
    }
}
