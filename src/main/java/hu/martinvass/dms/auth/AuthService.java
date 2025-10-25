package hu.martinvass.dms.auth;

import hu.martinvass.dms.auth.event.UserRegisteredEvent;
import hu.martinvass.dms.auth.verification.VerificationResult;
import hu.martinvass.dms.auth.verification.VerificationTokenRepository;
import hu.martinvass.dms.user.AppUser;
import hu.martinvass.dms.user.exception.UserAlreadyExistsException;
import hu.martinvass.dms.user.repository.AppUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
 * Service implementation for managing application users.
 */
@Service
@AllArgsConstructor
public class AuthService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG =
            "user with username %s not found";

    private final ApplicationEventPublisher eventPublisher;

    private final AppUserRepository appUserRepository;
    private final VerificationTokenRepository tokenRepository;

    private final Argon2PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(AppUser user) {
        // Validating if the provided user is unique
        validateUser(user);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        appUserRepository.save(user);

        // Publish event when user is registered
        eventPublisher.publishEvent(new UserRegisteredEvent(user));
    }

    @Transactional
    public VerificationResult verifyUser(String token) {
        var verificationToken = tokenRepository.findByToken(token)
                .orElse(null);

        // Token null -> invalid
        if (verificationToken == null) {
            return VerificationResult.INVALID;
        }

        // Token expired
        if (verificationToken.getExpiryDate().before(new Date())) {
            tokenRepository.delete(verificationToken);
            return VerificationResult.EXPIRED;
        }

        AppUser user = verificationToken.getUser();
        if (!user.isVerified()) {
            user.setVerified(true);
            appUserRepository.save(user);
        }

        // Verification was success -> delete the record since we no longer need it
        tokenRepository.delete(verificationToken);
        return VerificationResult.SUCCESS;
    }

    /**
     * Finds an application user by their username.
     *
     * @param username The username of the user to find.
     * @return An Optional containing the found AppUser, or an empty Optional if not found.
     */
    @Transactional(readOnly = true)
    public Optional<AppUser> findByUsername(String username) {
        return appUserRepository.findByProfile_Username(username);
    }

    /**
     * Finds an application user by their email.
     *
     * @param email The email of the user to find.
     * @return An Optional containing the found AppUser, or an empty Optional if not found.
     */
    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByProfile_Email(email);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return appUserRepository.findByProfile_Username(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                String.format(USER_NOT_FOUND_MSG, username)));
    }

    @Transactional(readOnly = true)
    public void validateUser(AppUser user) throws UserAlreadyExistsException {
        var userExistsByEmail = appUserRepository
                .findByProfile_Email(user.getProfile().getEmail())
                .isPresent();

        var userExistsByUsername = appUserRepository
                .findByProfile_Username(user.getUsername())
                .isPresent();

        if (userExistsByEmail || userExistsByUsername) {
            var message = new StringBuilder("User already exists with ");

            if (userExistsByUsername) message.append("username");
            if (userExistsByEmail) {
                if (userExistsByUsername) message.append(" and ");
                message.append("email");
            }

            throw new UserAlreadyExistsException(message.toString());
        }
    }
}