package hu.martinvass.dms.events.listeners;

import hu.martinvass.dms.audit.AuditEventAction;
import hu.martinvass.dms.audit.service.AuditService;
import hu.martinvass.dms.email.EmailService;
import hu.martinvass.dms.events.CorporationCreatedEvent;
import hu.martinvass.dms.events.InvitationCreatedEvent;
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
    private final EmailService emailService;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvitationCreatedEvent(InvitationCreatedEvent event) {
        var invitation = event.getInvitation();

        // Send email
        emailService.sendEmail(
                invitation.getInvitedEmail(),
                String.format("You have been invited to join %s", invitation.getCorporation().getName()),
                String.format("""
                    Hello,

                    You have been invited to join corporation: %s.
                    Invitation code: %s
                    Link: http://localhost:8080/invite/%s
                    This invitation expires on %s.
                    """,
                        invitation.getCorporation().getName(),
                        invitation.getCode(),
                        invitation.getCode(),
                        invitation.getExpiresAt().toString())
        );

        // Audit log
        auditService.log(
                AuditEventAction.INVITATION_CREATED,
                event.getUser(),
                String.format("Invitation created: %s | User: %s",
                        event.getUser().getActiveProfile().getCorporation().getName(),
                        event.getUser().getUsername())
        );
    }
}