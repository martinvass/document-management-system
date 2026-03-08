package hu.martinvass.dms.events.listeners;

import hu.martinvass.dms.events.CorporationCreatedEvent;
import hu.martinvass.dms.events.InvitationCreatedEvent;
import hu.martinvass.dms.shared.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@AllArgsConstructor
public class CorporationEventListener {

    private final EmailService emailService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCorporationCreatedEvent(CorporationCreatedEvent event) {
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvitationCreatedEvent(InvitationCreatedEvent event) {
        var invitation = event.getInvitation();

        emailService.sendInvitationEmail(invitation.getInvitedEmail(), invitation.getCode(),
                invitation.getCorporation().getName(), invitation.getExpiresAt().toString());
    }
}