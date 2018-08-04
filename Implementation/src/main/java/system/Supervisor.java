package system;

public interface Supervisor {

    /**
     * Generates a login key for the given agent.
     *
     * @param agentId The id of the given agent.
     * @return A 10-character randomly generated login key if the supervisor
     * decides that the agent can log in. Returns null otherwise.
     */
    String getLoginKey(final String agentId);
}
