package hu.martinvass.dms.audit;

import hu.martinvass.dms.audit.event.AuditEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuditService {

    private final ApplicationEventPublisher eventPublisher;

    public void log(AuditEventAction action, Long userId, String details) {
        eventPublisher.publishEvent(new AuditEvent(action, userId, details));
    }
}