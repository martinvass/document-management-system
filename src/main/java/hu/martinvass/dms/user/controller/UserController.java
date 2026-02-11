package hu.martinvass.dms.user.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.auth.AuthService;
import hu.martinvass.dms.corporation.document.service.DocumentService;
import hu.martinvass.dms.dto.CreateCorporationDto;
import hu.martinvass.dms.dto.JoinCorporationDto;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.utils.FileUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class UserController {

    private final CorporationProfileRepository corporationProfileRepository;

    private final DocumentService documentService;
    private final AuthService authService;

    @GetMapping("/home")
    public String home(@ActiveUserProfile CorporationProfile activeProfile, Model model, Principal principal) {
        var user = authService.findByUsername(principal.getName());

        var profiles = corporationProfileRepository.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("profiles", profiles);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute("activeProfileId", activeProfile == null ? -1 : activeProfile.getId());
        model.addAttribute("createDto", new CreateCorporationDto());
        model.addAttribute("joinDto", new JoinCorporationDto());

        if (activeProfile != null) {
            long totalBytes = documentService.getTotalStorageUsed(activeProfile);
            String readableSize = FileUtils.humanReadableSize(totalBytes);

            model.addAttribute("totalStorageBytes", totalBytes);
            model.addAttribute("totalStorageReadable", readableSize);
        } else {
            model.addAttribute("totalStorageBytes", 0L);
            model.addAttribute("totalStorageReadable", "0 B");
        }

        return "home";
    }
}