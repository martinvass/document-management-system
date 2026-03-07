package hu.martinvass.dms.corporation.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.annotations.RequireCorpAdmin;
import hu.martinvass.dms.corporation.domain.CorporationRole;
import hu.martinvass.dms.corporation.settings.service.GeneralSettingsService;
import hu.martinvass.dms.corporation.settings.service.StorageSettingsService;
import hu.martinvass.dms.invitation.Invitation;
import hu.martinvass.dms.invitation.dto.CreateInvitationDto;
import hu.martinvass.dms.invitation.service.InvitationService;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.shared.controller.BaseController;
import hu.martinvass.dms.shared.utils.PageUtils;
import hu.martinvass.dms.user.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/corporation/admin")
public class CorporationAdminController extends BaseController {

    private final StorageSettingsService storageSettingsService;
    private final GeneralSettingsService generalSettingsService;
    private final InvitationService invitationService;

    public CorporationAdminController(AppUserRepository userRepository, CorporationProfileRepository profileRepository, StorageSettingsService storageSettingsService, GeneralSettingsService generalSettingsService, InvitationService invitationService) {
        super(userRepository, profileRepository);

        this.storageSettingsService = storageSettingsService;
        this.generalSettingsService = generalSettingsService;
        this.invitationService = invitationService;
    }

    @GetMapping("/invitations")
    @RequireCorpAdmin
    public String invitations(@RequestParam(defaultValue = "1") int page,
                              @ActiveUserProfile CorporationProfile activeProfile,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String role,
                              Model model,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        // Safety check
        if (activeProfile.getCorporation() == null)
            return "redirect:/home";

        // Add basic attributes
        addBaseAttributes(activeProfile, model, principal);

        var requestedPage = Math.max(1, page);
        Page<Invitation> invitations;

        if (status != null || role != null) {
            invitations = invitationService.findFiltered(
                    activeProfile.getCorporation(),
                    status,
                    role,
                    PageRequest.of(page - 1, 5)
            );
        } else {
            invitations = invitationService.findByCorporation(
                    activeProfile.getCorporation().getId(),
                    PageRequest.of(page - 1, 5)
            );
        }

        var currentPage = PageUtils.safePageIndex(requestedPage, invitations);
        if (currentPage != requestedPage) {
            redirectAttributes.addAttribute("page", currentPage);
            return "redirect:/corporation/admin/invitations";
        }

        // Stats
        var stats = invitationService.getStatsForCorporation(activeProfile.getCorporation().getId());

        // Other attributes
        model.addAttribute("invitations", invitations);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("stats", stats);
        model.addAttribute("availableRoles", CorporationRole.values());
        model.addAttribute("createInvitationDto", new CreateInvitationDto());

        return "corporation/admin/invitations";
    }

    @GetMapping("/settings")
    @RequireCorpAdmin
    public String settings(@ActiveUserProfile CorporationProfile activeProfile,
                           Model model,
                           Principal principal) {
        // Safety check
        if (activeProfile.getCorporation() == null)
            return "redirect:/home";

        // Add basic attributes
        addBaseAttributes(activeProfile, model, principal);

        // Load settings
        model.addAttribute("general",
                generalSettingsService.get(activeProfile.getCorporation().getId()));

        model.addAttribute("storage",
                storageSettingsService.get(activeProfile.getCorporation().getId()));

        return "corporation/admin/settings";
    }
}