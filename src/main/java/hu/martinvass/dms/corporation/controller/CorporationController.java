package hu.martinvass.dms.corporation.controller;

import hu.martinvass.dms.dto.CreateCorporationDto;
import hu.martinvass.dms.corporation.service.CorporationService;
import hu.martinvass.dms.profile.ProfileSessionService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@AllArgsConstructor
@RequestMapping("/corporation")
public class CorporationController {

    private final CorporationService corporationService;
    private final ProfileSessionService sessionService;

    @PostMapping("/create")
    public String handleCreate(@ModelAttribute("createDto") CreateCorporationDto dto,
                               Principal principal,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        try {
            corporationService.createCorporation(dto, principal.getName(), session);
            redirectAttributes.addFlashAttribute("message", "Corporation created");

            // TODO: redirect to company dashboard or something
            return "redirect:/home";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/home";
        }
    }

    /**
     * Sign out from current corporation (not from application)
     * Clears active profile from session and redirects to home
     */
    @GetMapping("/sign-out")
    public String signOutFromCorporation(HttpSession session, RedirectAttributes redirectAttributes) {
        // Remove active profile from session
        sessionService.clear(session);

        redirectAttributes.addFlashAttribute("success", "You have successfully signed out the company.");
        // Redirect to home (corporation selection screen)
        return "redirect:/home";
    }
}