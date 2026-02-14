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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/documents")
@AllArgsConstructor
public class DocumentController {

    private final CorporationProfileRepository corporationProfileRepository;
    private final DocumentService documentService;
    //private final TagService tagService;
    private final AuthService authService;

    /**
     * Upload a new document
     */
    @PostMapping("/upload")
    public String upload(
            @ActiveUserProfile CorporationProfile profile,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            //@RequestParam(value = "tags", required = false) String tagsStr,
            RedirectAttributes ra
    ) {
        try {
            // Parse tags from comma-separated string
            Set<String> tags = new HashSet<>();
            /*if (tagsStr != null && !tagsStr.isBlank()) {
                tags = Arrays.stream(tagsStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());
            }*/

            documentService.upload(profile, file, description, tags);
            ra.addFlashAttribute("success", "Document uploaded successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/documents";
    }

    /**
     * List all documents
     */
    @GetMapping({"", "/"})
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

        // Get documents
        List<Document> documents = documentService.listDocuments(profile);
        model.addAttribute("documents", documents);

        // Get all tags for the corporation
        //model.addAttribute("availableTags", tagService.getAllTags(profile.getCorporation()));

        return "documents/list";
    }

    /**
     * View document details
     */
    @GetMapping("/{id}")
    public String view(
            @ActiveUserProfile CorporationProfile profile,
            @PathVariable Long id,
            Model model,
            Principal principal,
            RedirectAttributes ra
    ) {
        try {
            var user = authService.findByUsername(principal.getName());
            var profiles = corporationProfileRepository.findByUserId(user.getId());

            addBaseAttributes(profile, model, user, profiles);

            Document document = documentService.getDocument(id, profile);
            model.addAttribute("document", document);

            model.addAttribute("currentStorageType",
                    documentService.getCurrentStorageType(profile.getCorporation().getId()));

            // Get versions
            List<Document> versions = documentService.getVersions(id, profile);
            model.addAttribute("versions", versions);

            // Get available tags
            //model.addAttribute("availableTags", tagService.getAllTags(profile.getCorporation()));

            return "documents/view";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/documents/list";
        }
    }

    /**
     * Update document metadata
     */
    @PostMapping("/{id}/update")
    public String update(
            @ActiveUserProfile CorporationProfile profile,
            @PathVariable Long id,
            @RequestParam(value = "description", required = false) String description,
            //@RequestParam(value = "tags", required = false) String tagsStr,
            RedirectAttributes ra
    ) {
        try {
            // Parse tags
            Set<String> tags = new HashSet<>();
            /*if (tagsStr != null && !tagsStr.isBlank()) {
                tags = Arrays.stream(tagsStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());
            }*/

            documentService.updateDocument(id, profile, description, tags);
            ra.addFlashAttribute("success", "Document updated successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/documents/" + id;
    }

    @PostMapping("/{id}/migrate")
    public String migrate(
            @ActiveUserProfile CorporationProfile profile,
            @PathVariable Long id,
            RedirectAttributes ra
    ) {
        try {
            documentService.migrateDocument(id, profile);
            ra.addFlashAttribute("success", "Document migrated successfully to current storage");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Migration failed: " + e.getMessage());
        }

        return "redirect:/documents/" + id;
    }

    /**
     * Archive a document
     */
    @PostMapping("/{id}/archive")
    public String archive(
            @ActiveUserProfile CorporationProfile profile,
            @PathVariable Long id,
            RedirectAttributes ra
    ) {
        try {
            documentService.archiveDocument(id, profile);
            ra.addFlashAttribute("success", "Document archived successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/documents";
    }

    /**
     * Upload a new version
     */
    @PostMapping("/{id}/new-version")
    public String uploadNewVersion(
            @ActiveUserProfile CorporationProfile profile,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes ra
    ) {
        try {
            documentService.uploadNewVersion(id, profile, file);
            ra.addFlashAttribute("success", "New version uploaded successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/documents/" + id;
    }

    /**
     * Set a specific version as the latest
     */
    @PostMapping("/{id}/set-as-latest")
    public String setAsLatest(
            @ActiveUserProfile CorporationProfile profile,
            @PathVariable Long id,
            RedirectAttributes ra
    ) {
        try {
            Document updated = documentService.setAsLatest(id, profile);
            ra.addFlashAttribute("success", "Version set as latest successfully");
            return "redirect:/documents/" + updated.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/documents/" + id;
        }
    }

    /**
     * Download document
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @ActiveUserProfile CorporationProfile profile,
            @PathVariable Long id
    ) throws IOException {

        Document doc = documentService.getDocument(id, profile);

        InputStream is = documentService.download(id, profile);

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