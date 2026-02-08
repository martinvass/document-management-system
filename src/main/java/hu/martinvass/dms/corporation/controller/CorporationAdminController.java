package hu.martinvass.dms.corporation.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.annotations.RequireCorpAdmin;
import hu.martinvass.dms.auth.AuthService;
import hu.martinvass.dms.corporation.domain.CorporationRole;
import hu.martinvass.dms.corporation.settings.service.GeneralSettingsService;
import hu.martinvass.dms.corporation.settings.service.StorageSettingsService;
import hu.martinvass.dms.corporation.settings.dto.GeneralSettingsDto;
import hu.martinvass.dms.corporation.settings.dto.StorageSettingsDto;
import hu.martinvass.dms.dto.CreateCorporationDto;
import hu.martinvass.dms.dto.JoinCorporationDto;
import hu.martinvass.dms.invitation.Invitation;
import hu.martinvass.dms.dto.CreateInvitationDto;
import hu.martinvass.dms.dto.InvitationStatsDto;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/corporation/admin")
public class CorporationAdminController {

    private CorporationProfileRepository corporationProfileRepository;

    private final StorageSettingsService storageSettingsService;
    private final GeneralSettingsService generalSettingsService;

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
                PageRequest.of(requestedPage - 1, 5)
        );

        var currentPage = PageUtils.safePageIndex(requestedPage, invitations);
        if (currentPage != requestedPage) {
            redirectAttributes.addAttribute("page", currentPage);
            return "redirect:/corporation/admin/invitations";
        }

        // Stats
        InvitationStatsDto stats = invitationService.getStatsForCorporation(activeProfile.getCorporation().getId());

        // Other attributes
        model.addAttribute("invitations", invitations);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("stats", stats);
        model.addAttribute("availableRoles", CorporationRole.values());

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

        var user = authService.findByUsername(principal.getName());
        var profiles = corporationProfileRepository.findByUserId(user.getId());

        // Add basic attributes
        addBaseAttributes(activeProfile, model, user, profiles);

        // Load settings
        model.addAttribute("general",
                generalSettingsService.get(activeProfile.getCorporation().getId()));

        model.addAttribute("storage",
                storageSettingsService.get(activeProfile.getCorporation().getId()));

        return "corporation/admin/settings";
    }

    @PostMapping("/settings/general")
    @RequireCorpAdmin
    public String saveGeneral(
            @ActiveUserProfile CorporationProfile activeProfile,
            @ModelAttribute("general") GeneralSettingsDto dto,
            RedirectAttributes ra
    ) {
        Long companyId = activeProfile.getCorporation().getId();

        generalSettingsService.save(companyId, dto);

        ra.addFlashAttribute("successMessage", "Settings saved");
        return "redirect:/corporation/admin/settings";
    }

    @PostMapping("/settings/storage")
    @RequireCorpAdmin
    public String saveStorage(
            @ActiveUserProfile CorporationProfile activeProfile,
            @ModelAttribute("storage") StorageSettingsDto dto,
            RedirectAttributes ra
    ) {
        Long companyId = activeProfile.getCorporation().getId();

        storageSettingsService.save(companyId, dto);

        ra.addFlashAttribute("successMessage", "Settings saved");
        return "redirect:/corporation/admin/settings";
    }

    @PostMapping("/settings/storage/test")
    @RequireCorpAdmin
    public String testStorageConnection(
            @ActiveUserProfile CorporationProfile activeProfile,
            @ModelAttribute("storage") StorageSettingsDto dto,
            RedirectAttributes ra
    ) {
        Long companyId = activeProfile.getCorporation().getId();

        try {
            storageSettingsService.testConnection(companyId, dto);
            ra.addFlashAttribute("storageTestSuccess", true);
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("storageTestError", ex.getMessage());
        }

        return "redirect:/corporation/admin/settings";
    }

    private void addBaseAttributes(CorporationProfile activeProfile, Model model, AppUser user, List<CorporationProfile> profiles) {
        model.addAttribute("user", user);
        model.addAttribute("profiles", profiles);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute("activeProfileId", activeProfile.getId());

        model.addAttribute("createDto", new CreateCorporationDto());
        model.addAttribute("joinDto", new JoinCorporationDto());
        model.addAttribute("createInvitationDto", new CreateInvitationDto());
    }
}