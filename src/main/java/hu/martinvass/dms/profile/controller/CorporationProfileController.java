package hu.martinvass.dms.profile.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.corporation.domain.CorporationRole;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.dto.SwitchProfileDto;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.profile.service.CorporationProfileService;
import hu.martinvass.dms.user.repository.AppUserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class CorporationProfileController {

    private final CorporationProfileService corporationProfileService;

    private final AppUserRepository userRepository;
    private final CorporationProfileRepository profileRepository;

    @PostMapping("/profile/switch")
    public String switchProfile(@ModelAttribute SwitchProfileDto dto,
                                Principal principal,
                                HttpSession session,
                                RedirectAttributes ra) {
        try {
            corporationProfileService.switchActiveProfile(dto.profileId(), principal.getName(), session);
            ra.addFlashAttribute("message", "Switched profile successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/home";
    }

    @GetMapping("/profile/view")
    public String viewProfile(
            @ActiveUserProfile CorporationProfile activeProfile,
            Principal principal,
            Model model
    ) {
        var user = userRepository.findByProfile_Username(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var allProfiles = profileRepository.findByUserId(user.getId());

        var adminCount = allProfiles.stream()
                .filter(p -> p.getRole() == CorporationRole.ADMIN)
                .count();

        var employeeCount = allProfiles.stream()
                .filter(p -> p.getRole() == CorporationRole.EMPLOYEE)
                .count();

        model.addAttribute("user", user);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute("allProfiles", allProfiles);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("employeeCount", employeeCount);

        return "user/profile";
    }
}