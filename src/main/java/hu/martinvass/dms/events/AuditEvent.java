package hu.martinvass.dms.events;

import hu.martinvass.dms.audit.AuditEventAction;
import hu.martinvass.dms.user.AppUser;
import lombok.Getter;

import java.util.Date;

@Getter
public class AuditEvent {

    private final AuditEventAction action;
    private final AppUser user;
    private final String details;
    private final Date timestamp;

    public AuditEvent(AuditEventAction action, AppUser user, String details) {
        this.action = action;
        this.user = user;
        this.details = details;
        this.timestamp = new Date(System.currentTimeMillis());
    }
}