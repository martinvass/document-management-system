package hu.martinvass.dms.corporation.settings.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.annotations.RequireCorpAdmin;
import hu.martinvass.dms.corporation.settings.dto.GeneralSettingsDto;
import hu.martinvass.dms.corporation.settings.dto.StorageSettingsDto;
import hu.martinvass.dms.corporation.settings.service.GeneralSettingsService;
import hu.martinvass.dms.corporation.settings.service.StorageSettingsService;
import hu.martinvass.dms.profile.CorporationProfile;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@AllArgsConstructor
@Controller
@RequestMapping("/corporation/admin/settings")
public class CorporationSettingsController {

    private final StorageSettingsService storageSettingsService;
    private final GeneralSettingsService generalSettingsService;

    @PostMapping("/general")
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

    @PostMapping("/storage")
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

    @PostMapping("/storage/test")
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
}