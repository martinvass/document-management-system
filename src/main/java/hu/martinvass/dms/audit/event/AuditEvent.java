package hu.martinvass.dms.audit.event;

import hu.martinvass.dms.audit.AuditEventAction;
import lombok.Getter;

import java.util.Date;

@Getter
public class AuditEvent {

    private final AuditEventAction action;
    private final Long userId;
    private final String details;
    private final Date timestamp;

    public AuditEvent(AuditEventAction action, Long userId, String details) {
        this.action = action;
        this.userId = userId;
        this.details = details;
        this.timestamp = new Date(System.currentTimeMillis());
    }
}