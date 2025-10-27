package hu.martinvass.dms.invitation.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.annotations.RequireCorpAdmin;
import hu.martinvass.dms.invitation.data.CreateInvitationDTO;
import hu.martinvass.dms.invitation.service.InvitationService;
import hu.martinvass.dms.profile.CorporationProfile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class InvitationController {

    private final InvitationService invitationService;

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
}