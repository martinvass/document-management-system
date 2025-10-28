package hu.martinvass.dms.invitation.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.annotations.RequireCorpAdmin;
import hu.martinvass.dms.auth.AuthService;
import hu.martinvass.dms.data.CreateInvitationDTO;
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
    public String createInvitation(@Valid @ModelAttribute CreateInvitationDTO data,
                                   @ActiveUserProfile CorporationProfile profile,
                                   BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/corporation/admin/invitations?error";
        }

        this.invitationService.createInvitation(data, profile);

        return "redirect:/corporation/admin/invitations";
    }

    @PostMapping("/corporation/admin/invitations/{id}/revoke")
    @RequireCorpAdmin
    public String revokeInvitation(@PathVariable Long id) {
        // TODO: error handling: messages
        try {
            invitationService.revokeInvitation(id);

            return "redirect:/corporation/admin/invitations";
        } catch (Exception e) {
            return "redirect:/corporation/admin/invitations";
        }
    }

    @GetMapping("/invite/{code}")
    public String showInvitation(@PathVariable String code,
                                 Model model,
                                 Principal principal) {
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
            // TODO: manage error
            return "";
        }
    }

    @PostMapping("/invite/{code}/accept")
    public String acceptInvitation(@PathVariable String code,
                                   Principal principal,
                                   RedirectAttributes attributes) {
        try {
            // TODO: toasts messages using attributes
            var user = authService.findByUsername(principal.getName());

            invitationService.acceptInvitation(code, user);
            return "redirect:/home";
        } catch (Exception e) {
            // TODO: manage error
            return "";
        }
    }
}