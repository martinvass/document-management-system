package hu.martinvass.dms.audit;

public enum AuditEventAction {

    USER_REGISTERED,
    USER_LOGGED_IN,
    USER_LOGIN_FAILED,
    UNKNOWN_USER_LOGIN_ATTEMPT
}