package hu.martinvass.dms.profile.service;

import hu.martinvass.dms.profile.ProfileSessionService;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.user.repository.AppUserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CorporationProfileService {

    private final AppUserRepository userRepository;
    private final CorporationProfileRepository profileRepository;
    private final ProfileSessionService activeSession;

    @Transactional(readOnly = true)
    public void switchActiveProfile(Long userProfileId, String username, HttpSession session) {
        var appUser = userRepository.findByProfile_Username(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        var toActivate = profileRepository.findByIdAndUser_Profile_Username(userProfileId, username)
                .orElseThrow(() -> new RuntimeException("Profile ID " + userProfileId + " is not available for user " + username));

        appUser.setActiveProfile(toActivate);

        // Set in session
        activeSession.setActiveProfile(session, toActivate.getId());
    }
}