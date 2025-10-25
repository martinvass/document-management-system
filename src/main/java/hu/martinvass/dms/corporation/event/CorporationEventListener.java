package hu.martinvass.dms.corporation.event;

import hu.martinvass.dms.audit.AuditEventAction;
import hu.martinvass.dms.audit.service.AuditService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@AllArgsConstructor
public class CorporationEventListener {

    private final AuditService auditService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCorporationCreatedEvent(CorporationCreatedEvent event) {
        // Audit log
        auditService.log(
                AuditEventAction.CORPORATION_CREATED,
                event.getUser(),
                String.format("Corporation created: %s | User: %s",
                        event.getCorporation().getName(),
                        event.getUser().getUsername())
        );
    }
}