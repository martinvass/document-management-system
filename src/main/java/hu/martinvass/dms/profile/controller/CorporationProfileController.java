package hu.martinvass.dms.profile.controller;

import hu.martinvass.dms.dto.SwitchProfileDto;
import hu.martinvass.dms.profile.service.CorporationProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class CorporationProfileController {

    private final CorporationProfileService corporationProfileService;

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
}