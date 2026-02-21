package hu.martinvass.dms.corporation.document.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.corporation.document.domain.Document;
import hu.martinvass.dms.corporation.document.domain.DocumentPermissionLevel;
import hu.martinvass.dms.corporation.document.repository.DocumentRepository;
import hu.martinvass.dms.corporation.document.service.DocumentPermissionService;
import hu.martinvass.dms.corporation.document.service.DocumentService;
import hu.martinvass.dms.department.domain.Department;
import hu.martinvass.dms.department.service.DepartmentService;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/documents/{docId}/permissions")
@AllArgsConstructor
public class DocumentPermissionController {

    private final DocumentService documentService;
    private final DocumentPermissionService permissionService;
    private final DepartmentService departmentService;
    private final DocumentRepository documentRepository;
    private final CorporationProfileRepository profileRepository;

    /**
     * Add document to department
     */
    @PostMapping("/add-department")
    public String addDepartment(
            @ActiveUserProfile CorporationProfile profile,
            @PathVariable Long docId,
            @RequestParam Long departmentId,
            RedirectAttributes ra
    ) {
        try {
            Document document = documentService.getDocument(docId, profile);

            // Check ADMIN permission
            permissionService.checkPermission(document, profile, DocumentPermissionLevel.ADMIN);

            Department department = departmentService.getDepartment(departmentId, profile.getCorporation());
            document.getDepartments().add(department);
            documentRepository.save(document);

            ra.addFlashAttribute("success", "Document added to department successfully");
        } catch (SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to add document to department: " + e.getMessage());
        }

        return "redirect:/documents/" + docId;
    }

    /**
     * Remove document from department
     */
    @PostMapping("/remove-department")
    public String removeDepartment(
            @ActiveUserProfile CorporationProfile profile,
            @PathVariable Long docId,
            @RequestParam Long departmentId,
            RedirectAttributes ra
    ) {
        try {
            Document document = documentService.getDocument(docId, profile);

            // Check ADMIN permission
            permissionService.checkPermission(document, profile, DocumentPermissionLevel.ADMIN);

            Department department = departmentService.getDepartment(departmentId, profile.getCorporation());
            document.getDepartments().remove(department);
            documentRepository.save(document);

            ra.addFlashAttribute("success", "Document removed from department successfully");
        } catch (SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to remove document from department: " + e.getMessage());
        }

        return "redirect:/documents/" + docId;
    }

    /**
     * Grant permission to user
     */
    @PostMapping("/grant-user")
    public String grantUserPermission(
            @ActiveUserProfile CorporationProfile profile,
            @PathVariable Long docId,
            @RequestParam Long profileId,
            @RequestParam String permissionLevel,
            RedirectAttributes ra
    ) {
        try {
            Document document = documentService.getDocument(docId, profile);
            CorporationProfile targetProfile = profileRepository.findById(profileId)
                    .orElseThrow(() -> new RuntimeException("User profile not found"));

            // Verify same corporation
            if (!targetProfile.getCorporation().getId().equals(profile.getCorporation().getId())) {
                throw new SecurityException("Cannot grant permissions across corporations");
            }

            DocumentPermissionLevel level = DocumentPermissionLevel.valueOf(permissionLevel);
            permissionService.grantPermission(document, targetProfile, level, profile);

            ra.addFlashAttribute("success", "Permission granted successfully");
        } catch (SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to grant permission: " + e.getMessage());
        }

        return "redirect:/documents/" + docId;
    }

    /**
     * Revoke permission from user
     */
    @PostMapping("/revoke-user")
    public String revokeUserPermission(
            @ActiveUserProfile CorporationProfile profile,
            @PathVariable Long docId,
            @RequestParam Long profileId,
            RedirectAttributes ra
    ) {
        try {
            Document document = documentService.getDocument(docId, profile);
            CorporationProfile targetProfile = profileRepository.findById(profileId)
                    .orElseThrow(() -> new RuntimeException("Profile not found"));

            permissionService.revokePermission(document, targetProfile, profile);

            ra.addFlashAttribute("success", "Permission revoked successfully");
        } catch (SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to revoke permission: " + e.getMessage());
        }

        return "redirect:/documents/" + docId;
    }
}