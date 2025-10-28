package hu.martinvass.dms.corporation.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.annotations.RequireCorpAdmin;
import hu.martinvass.dms.auth.AuthService;
import hu.martinvass.dms.corporation.CorporationRole;
import hu.martinvass.dms.data.CreateCorporationDTO;
import hu.martinvass.dms.data.JoinCorporationDTO;
import hu.martinvass.dms.invitation.Invitation;
import hu.martinvass.dms.data.CreateInvitationDTO;
import hu.martinvass.dms.data.InvitationStatsDTO;
import hu.martinvass.dms.invitation.service.InvitationService;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.user.AppUser;
import hu.martinvass.dms.utils.PageUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/corporation/admin")
public class CorporationAdminController {

    private CorporationProfileRepository corporationProfileRepository;

    private final InvitationService invitationService;
    private final AuthService authService;

    @GetMapping("/invitations")
    @RequireCorpAdmin
    public String invitations(@RequestParam(defaultValue = "1") int page,
                              @ActiveUserProfile CorporationProfile activeProfile,
                              Model model,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        // Safety check
        if (activeProfile.getCorporation() == null)
            return "redirect:/home";

        var user = authService.findByUsername(principal.getName());
        var profiles = corporationProfileRepository.findByUserId(user.getId());

        // Add basic attributes
        addBaseAttributes(activeProfile, model, user, profiles);

        // Fetching invitations based on corporation
        var requestedPage = Math.max(1, page);
        Page<Invitation> invitations = invitationService.findByCorporation(
                activeProfile.getCorporation().getId(),
                PageRequest.of(requestedPage - 1, 2)
        );

        var currentPage = PageUtils.safePageIndex(requestedPage, invitations);
        if (currentPage != requestedPage) {
            redirectAttributes.addAttribute("page", currentPage);
            return "redirect:/corporation/admin/invitations";
        }

        // Stats
        InvitationStatsDTO stats = invitationService.getStatsForCorporation(activeProfile.getCorporation().getId());

        // Other attributes
        model.addAttribute("invitations", invitations);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("stats", stats);
        model.addAttribute("availableRoles", CorporationRole.values());

        return "corporation/admin/invitations";
    }

    private void addBaseAttributes(CorporationProfile activeProfile, Model model, AppUser user, List<CorporationProfile> profiles) {
        model.addAttribute("user", user);
        model.addAttribute("profiles", profiles);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute("activeProfileId", activeProfile.getId());

        model.addAttribute("createDto", new CreateCorporationDTO());
        model.addAttribute("joinDto", new JoinCorporationDTO());
        model.addAttribute("createInvitationDto", new CreateInvitationDTO());
    }
}