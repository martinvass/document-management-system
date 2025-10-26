package hu.martinvass.dms.corporation.service;

import hu.martinvass.dms.profile.ProfileSessionService;
import hu.martinvass.dms.corporation.Corporation;
import hu.martinvass.dms.corporation.CorporationRole;
import hu.martinvass.dms.corporation.data.CreateCorporationDTO;
import hu.martinvass.dms.corporation.event.CorporationCreatedEvent;
import hu.martinvass.dms.corporation.repository.CorporationRepository;
import hu.martinvass.dms.user.AppUser;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.user.repository.AppUserRepository;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CorporationService {

    private final CorporationRepository corporationRepository;
    private final AppUserRepository appUserRepository;
    private final CorporationProfileRepository profileRepository;

    private final ProfileSessionService activeSession;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void createCorporation(CreateCorporationDTO dto, String username, HttpSession session) {
        // Validation
        if (corporationRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Corporation with name '" + dto.getName() + "' already exists."); // Specifikusabb kivétel
        }

        var creatorUser = appUserRepository.findByProfile_Username(username)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found."));

        var profile = createCorpInternal(dto, creatorUser);

        // Set in session
        activeSession.setActiveProfile(session, profile.getId());

        // Fire event for audit log and other
        applicationEventPublisher.publishEvent(new CorporationCreatedEvent(creatorUser, profile.getCorporation()));
    }

    private CorporationProfile createCorpInternal(CreateCorporationDTO dto, AppUser creatorUser) {
        // Create and save corporation
        var corp = Corporation.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .owner(creatorUser)
                .build();

        corporationRepository.save(corp); // Save

        // Create and save user profile for the corporation
        var profile = CorporationProfile.builder()
                .user(creatorUser)
                .profile(creatorUser.getProfile())
                .corporation(corp)
                .role(CorporationRole.ADMIN)
                .build();

        profileRepository.save(profile); // Save

        creatorUser.getProfiles().add(profile);

        return profile;
    }
}