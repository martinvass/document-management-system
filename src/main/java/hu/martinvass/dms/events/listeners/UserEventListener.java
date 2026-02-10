package hu.martinvass.dms.events.listeners;

import hu.martinvass.dms.events.UserRegisteredEvent;
import hu.martinvass.dms.auth.verification.VerificationToken;
import hu.martinvass.dms.auth.verification.VerificationTokenRepository;
import hu.martinvass.dms.email.EmailService;
import hu.martinvass.dms.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final EmailService emailService;

    private final AppUserRepository appUserRepository;
    private final VerificationTokenRepository tokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        var user = event.getUser();
        var email = user.getProfile().getEmail();

        // Send activation link through email
        var token = new VerificationToken(user);
        tokenRepository.save(token);

        sendActivationEmailAsync(email, token.getToken());
    }

    @Async
    public void sendActivationEmailAsync(String email, String token) {
        var verificationLink = "http://localhost:8080/auth/verify?token=" + token;
        emailService.sendEmail(
                email,
                "DMS - Activate account",
                "Click the link to activate your account: " + verificationLink
        );
    }
}