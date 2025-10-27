package hu.martinvass.dms.corporation;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.annotations.RequireCorpAdmin;
import hu.martinvass.dms.auth.AuthService;
import hu.martinvass.dms.invitation.Invitation;
import hu.martinvass.dms.invitation.data.CreateInvitationDTO;
import hu.martinvass.dms.invitation.data.InvitationStatsDTO;
import hu.martinvass.dms.invitation.service.InvitationService;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@Controller
public class CorporationAdminController {

    private CorporationProfileRepository corporationProfileRepository;

    private final InvitationService invitationService;
    private final AuthService authService;

    @GetMapping("/corporation/admin/invitations")
    @RequireCorpAdmin
    public String invitations(@ActiveUserProfile CorporationProfile activeProfile, Model model, Principal principal) {
        var user = authService.findByUsername(principal.getName());

        var profiles = corporationProfileRepository.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("profiles", profiles);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute("activeProfileId", activeProfile == null ? -1 : activeProfile.getId());

        model.addAttribute("createInvitationDto", new CreateInvitationDTO());

        List<Invitation> invitations = invitationService.findByCorporation(activeProfile.getCorporation().getId());
        InvitationStatsDTO stats = invitationService.getStatsForCorporation(activeProfile.getCorporation().getId());

        model.addAttribute("invitations", invitations);
        model.addAttribute("stats", stats);
        model.addAttribute("availableRoles", CorporationRole.values());

        return "corporation/admin/invitations";
    }
}