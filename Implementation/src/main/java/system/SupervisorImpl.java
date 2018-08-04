package system;

import util.Utils;

public class SupervisorImpl implements Supervisor {

    private static final int LOGIN_KEY_LENGTH = 10;

    private final MessagingSystem messagingSystem;

    public SupervisorImpl(MessagingSystem messagingSystem) {
        this.messagingSystem = messagingSystem;
    }

    /**
     * Checks if it is safe for the agent to login by making sure
     * the id is not that of a spy (starts with "spy-")
     *
     * @param agentId The id of the given agent.
     * @return null if the agent is a spy, a 10 character login key
     * that is randomly generated if the agent is not a spy.
     */
    public String getLoginKey(String agentId) {

        if (agentId.startsWith("spy-")) {
            return null;
        } else {
            final String loginKey = Utils.getNRandomCharacters(LOGIN_KEY_LENGTH);
            messagingSystem.registerLoginKey(agentId, loginKey);
            return loginKey;
        }
    }
}
