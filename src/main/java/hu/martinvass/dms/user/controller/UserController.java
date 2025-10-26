package hu.martinvass.dms.user.controller;

import hu.martinvass.dms.auth.AuthService;
import hu.martinvass.dms.config.resolver.ActiveUserProfile;
import hu.martinvass.dms.corporation.data.CreateCorporationDTO;
import hu.martinvass.dms.corporation.data.JoinCorporationDTO;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class UserController {

    private final CorporationProfileRepository corporationProfileRepository;
    private final AuthService authService;

    @GetMapping("/home")
    public String home(@ActiveUserProfile CorporationProfile activeProfile, Model model, Principal principal, HttpSession session) {
        var user = authService.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + principal.getName()));

        var profiles = corporationProfileRepository.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("profiles", profiles);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute("activeProfileId", activeProfile == null ? -1 : activeProfile.getId());
        model.addAttribute("createDto", new CreateCorporationDTO());
        model.addAttribute("joinDto", new JoinCorporationDTO());

        return "home";
    }
}