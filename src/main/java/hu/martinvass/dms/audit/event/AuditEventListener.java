package hu.martinvass.dms.audit.event;

import hu.martinvass.dms.audit.AuditLogEntry;
import hu.martinvass.dms.audit.AuditScope;
import hu.martinvass.dms.audit.repository.AuditLogRepository;
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
                event.getUser(),
                event.getDetails()
        );

        boolean companyScope = event.getUser() != null && event.getUser().isInCorporation() && event.getUser().getActiveProfile() != null;

        entry.setScope(companyScope ? AuditScope.COMPANY : AuditScope.GLOBAL);

        if (companyScope && event.getUser().getActiveProfile() != null)
            entry.setCorporation(event.getUser().getActiveProfile().getCorporation());

        repository.save(entry);
    }
}