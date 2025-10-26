package hu.martinvass.dms.audit.service;

import hu.martinvass.dms.audit.AuditEventAction;
import hu.martinvass.dms.audit.AuditLogEntry;
import hu.martinvass.dms.audit.event.AuditEvent;
import hu.martinvass.dms.audit.repository.AuditLogRepository;
import hu.martinvass.dms.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void log(AuditEventAction action, AppUser user, String details) {
        eventPublisher.publishEvent(new AuditEvent(action, user, details));
    }

    public List<AuditLogEntry> getLogEntries(AppUser user) {
        // Global-level accessing
        if (user.isSystemAdmin()) {
            return auditLogRepository.findAllByOrderByTimestampDesc().orElse(List.of());
        }

        // Company-level accessing
        if (user.isCorporationAdmin()) {
            return auditLogRepository.findByCorporationOrderByTimestampDesc(user.getActiveProfile().getCorporation()).orElse(List.of());
        }

        // Default
        return List.of();
    }
}