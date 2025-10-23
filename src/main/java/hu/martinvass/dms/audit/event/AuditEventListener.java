package hu.martinvass.dms.audit.event;

import hu.martinvass.dms.audit.AuditLogEntry;
import hu.martinvass.dms.audit.AuditLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository repository;

    @EventListener
    public void handleAuditEvent(AuditEvent event) {
        var entry = new AuditLogEntry(
                event.getAction(),
                event.getUserId(),
                event.getDetails()
        );
        repository.save(entry);
    }
}