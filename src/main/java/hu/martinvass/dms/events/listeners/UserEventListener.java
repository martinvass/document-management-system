package hu.martinvass.dms.events.listeners;

import hu.martinvass.dms.auth.verification.VerificationToken;
import hu.martinvass.dms.auth.verification.VerificationTokenRepository;
import hu.martinvass.dms.events.UserRegisteredEvent;
import hu.martinvass.dms.shared.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final EmailService emailService;

    private final VerificationTokenRepository tokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        var user = event.getUser();
        var email = user.getProfile().getEmail();

        // Send activation link through email
        var token = new VerificationToken(user);
        tokenRepository.save(token);

        emailService.sendVerificationEmail(email, token.getToken());
    }
}