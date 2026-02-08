package hu.martinvass.dms.corporation.document.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.auth.AuthService;
import hu.martinvass.dms.corporation.document.domain.Document;
import hu.martinvass.dms.corporation.document.service.DocumentService;
import hu.martinvass.dms.dto.CreateCorporationDto;
import hu.martinvass.dms.dto.CreateInvitationDto;
import hu.martinvass.dms.dto.JoinCorporationDto;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/documents")
@AllArgsConstructor
public class DocumentController {

    private CorporationProfileRepository corporationProfileRepository;

    private final DocumentService documentService;
    private final AuthService authService;

    @PostMapping("/upload")
    //@RequireCorpMember
    public String upload(
            @ActiveUserProfile CorporationProfile profile,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes ra
    ) {
        try {
            documentService.upload(
                    profile.getCorporation().getId(),
                    profile.getUser().getId(),
                    file
            );
            ra.addFlashAttribute("success", "Document uploaded");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/documents/list";
    }

    @GetMapping("/list")
    //@RequireCorpMember
    public String list(
            @ActiveUserProfile CorporationProfile profile,
            Model model,
            Principal principal
    ) {
        if (profile.getCorporation() == null)
            return "redirect:/home";

        var user = authService.findByUsername(principal.getName());
        var profiles = corporationProfileRepository.findByUserId(user.getId());

        // Add basic attributes
        addBaseAttributes(profile, model, user, profiles);

        model.addAttribute(
                "documents",
                documentService.list(profile.getCorporation().getId())
        );
        return "documents/list";
    }

    @GetMapping("/{id}/download")
    //@RequireCorpMember
    public ResponseEntity<Resource> download(
            @ActiveUserProfile CorporationProfile profile,
            @PathVariable Long id
    ) throws IOException {

        Document doc = documentService.getDocument(
                profile.getCorporation().getId(),
                id
        );

        InputStream is = documentService.download(
                profile.getCorporation().getId(),
                id
        );

        InputStreamResource resource = new InputStreamResource(is);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getOriginalFilename() + "\""
                )
                .body(resource);
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