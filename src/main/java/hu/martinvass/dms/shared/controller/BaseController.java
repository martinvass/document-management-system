package hu.martinvass.dms.shared.controller;

import hu.martinvass.dms.corporation.dto.CreateCorporationDto;
import hu.martinvass.dms.corporation.dto.JoinCorporationDto;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.user.repository.AppUserRepository;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.security.Principal;

@Component
public abstract class BaseController {

    protected final AppUserRepository userRepository;

    protected final CorporationProfileRepository profileRepository;

    public BaseController(AppUserRepository userRepository, CorporationProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Add common model attributes
     */
    protected void addBaseAttributes(
            CorporationProfile activeProfile,
            Model model,
            Principal principal
    ) {
        if (principal != null) {
            var user = userRepository.findByProfile_Username(principal.getName())
                    .orElse(null);

            if (user != null) {
                model.addAttribute("user", user);

                var profiles = profileRepository.findByUserId(user.getId());
                model.addAttribute("profiles", profiles);
            }
        }

        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute("activeProfileId", activeProfile != null ? activeProfile.getId() : -1L);
    }
}