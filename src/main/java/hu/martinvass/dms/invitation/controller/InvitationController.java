package hu.martinvass.dms.invitation.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.annotations.RequireCorpAdmin;
import hu.martinvass.dms.auth.AuthService;
import hu.martinvass.dms.dto.CreateInvitationDto;
import hu.martinvass.dms.invitation.service.InvitationService;
import hu.martinvass.dms.profile.CorporationProfile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class InvitationController {

    private final InvitationService invitationService;
    private final AuthService authService;

    @PostMapping("/corporation/admin/invitations/create")
    @RequireCorpAdmin
    public String createInvitation(@Valid @ModelAttribute CreateInvitationDto data,
                                   @ActiveUserProfile CorporationProfile profile,
                                   BindingResult result,
                                   RedirectAttributes attributes) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("error", "There were errors in your fields.");
            return "redirect:/corporation/admin/invitations";
        }

        this.invitationService.createInvitation(data, profile);

        attributes.addFlashAttribute("message", "Invitation created");
        return "redirect:/corporation/admin/invitations";
    }

    @PostMapping("/corporation/admin/invitations/{id}/revoke")
    @RequireCorpAdmin
    public String revokeInvitation(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            invitationService.revokeInvitation(id);

            attributes.addFlashAttribute("message", "Invitation revoked");
            return "redirect:/corporation/admin/invitations";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/corporation/admin/invitations";
        }
    }

    @GetMapping("/invite/{code}")
    public String showInvitation(@PathVariable String code,
                                 Model model,
                                 Principal principal,
                                 RedirectAttributes attributes) {
        try {
            var invitation = invitationService.findByCode(code);
            var expired = invitationService.isExpired(invitation);
            var user = authService.findByUsername(principal.getName());

            if (!invitation.getInvitedEmail().equalsIgnoreCase(user.getProfile().getEmail())) {
                return "redirect:/";
            }

            model.addAttribute("invitation", invitation);
            model.addAttribute("expired", expired);

            return "invite_accept";
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/home";
        }
    }

    @PostMapping("/invite/{code}/accept")
    public String acceptInvitation(@PathVariable String code,
                                   Principal principal,
                                   RedirectAttributes attributes) {
        try {
            var user = authService.findByUsername(principal.getName());

            invitationService.acceptInvitation(code, user);

            attributes.addFlashAttribute("message", "Invitation successfully accepted");
            return "redirect:/home";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/home";
        }
    }
}