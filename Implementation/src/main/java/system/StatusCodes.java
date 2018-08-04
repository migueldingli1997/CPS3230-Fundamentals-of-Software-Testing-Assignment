package system;

public enum StatusCodes {
    OK,                             // successful
    TARGET_AGENT_DOES_NOT_EXIST,    // caused by user
    MESSAGE_LENGTH_EXCEEDED,        // caused by user
    BOTH_AGENT_QUOTAS_EXCEEDED,     // caused by exceeded quota (causes both to logout)
    SOURCE_AGENT_QUOTA_EXCEEDED,    // caused by exceeded quota (causes source logout)
    TARGET_AGENT_QUOTA_EXCEEDED,    // caused by exceeded quota (causes target logout)
    SOURCE_AGENT_DOES_NOT_EXIST,    // caused by system error (causes logout)
    SOURCE_AGENT_NOT_LOGGED_IN,     // caused by system error (causes logout)
    SESSION_KEY_UNRECOGNIZED,       // caused by system error (causes logout)
    FAILED_TO_ADD_TO_MAILBOX,       // caused by system error (causes logout)
    GENERIC_ERROR                   // represents an error
}
