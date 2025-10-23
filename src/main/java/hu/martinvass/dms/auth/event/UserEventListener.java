package hu.martinvass.dms.auth.event;

import hu.martinvass.dms.audit.AuditService;
import hu.martinvass.dms.auth.verification.VerificationToken;
import hu.martinvass.dms.auth.verification.VerificationTokenRepository;
import hu.martinvass.dms.email.EmailService;
import hu.martinvass.dms.user.AppUser;
import hu.martinvass.dms.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final EmailService emailService;
    private final AuditService auditService;

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

        // Audit log
        auditService.log("USER_REGISTERED", user.getId(), "User registered: " + email);
    }

    @EventListener
    public void onUserLoggedIn(InteractiveAuthenticationSuccessEvent event) {
        var principal = event.getAuthentication().getPrincipal();
        var details = event.getAuthentication().getDetails();

        if (principal instanceof AppUser user && details instanceof WebAuthenticationDetails webDetails) {
            // Audit log
            auditService.log(
                    "USER_LOGGED_IN",
                    user.getId(),
                    String.format("Logged in: %s | IP: %s", user.getUsername(), webDetails.getRemoteAddress())
            );
        }

        System.out.println("ad");
    }

    @EventListener
    public void onLoginFailure(AuthenticationFailureBadCredentialsEvent event) {
        var principal = event.getAuthentication().getPrincipal();
        var details = event.getAuthentication().getDetails();

        var username = (principal instanceof AppUser appUser)
                ? appUser.getUsername()
                : principal.toString();

        var ip = (details instanceof WebAuthenticationDetails webDetails)
                ? webDetails.getRemoteAddress()
                : "unknown";

        boolean exists = appUserRepository.findByProfile_Username(username).isPresent();

        if (exists) {
            auditService.log("USER_LOGIN_FAILED", null, "Bad password: " + username + " | IP: " + ip);
        } else {
            auditService.log("UNKNOWN_USER_LOGIN_ATTEMPT", null, "Username not found: " + username + " | IP: " + ip);
        }
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