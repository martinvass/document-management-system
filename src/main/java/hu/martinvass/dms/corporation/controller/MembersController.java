package hu.martinvass.dms.corporation.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.auth.AuthService;
import hu.martinvass.dms.corporation.document.domain.Document;
import hu.martinvass.dms.corporation.document.service.DocumentService;
import hu.martinvass.dms.department.domain.Department;
import hu.martinvass.dms.department.service.DepartmentService;
import hu.martinvass.dms.dto.CreateCorporationDto;
import hu.martinvass.dms.dto.JoinCorporationDto;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.user.AppUser;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/corporation/members")
@AllArgsConstructor
public class MembersController {

    private final CorporationProfileRepository profileRepository;
    private final DepartmentService departmentService;
    private final DocumentService documentService;
    private final AuthService authService;

    /**
     * List all members (paginated)
     */
    @GetMapping
    public String list(
            @ActiveUserProfile CorporationProfile activeProfile,
            @RequestParam(defaultValue = "1") int page,
            Model model,
            Principal principal
    ) {
        AppUser user = authService.findByUsername(principal.getName());
        List<CorporationProfile> profiles = profileRepository.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute("activeProfileId", activeProfile.getId());
        model.addAttribute("profiles", profiles);
        model.addAttribute("createDto", new CreateCorporationDto());
        model.addAttribute("joinDto", new JoinCorporationDto());

        // Get members (paginated)
        Page<CorporationProfile> members = profileRepository.findByCorporation(
                activeProfile.getCorporation(),
                PageRequest.of(page - 1, 15)
        );

        model.addAttribute("members", members);
        model.addAttribute("currentPage", page);

        // Get departments for assignment dropdown
        List<Department> departments = departmentService.getAllDepartments(activeProfile.getCorporation());
        model.addAttribute("departments", departments);

        return "corporation/members/list";
    }

    /**
     * View member details
     */
    @GetMapping("/{id}")
    public String view(
            @ActiveUserProfile CorporationProfile activeProfile,
            @PathVariable Long id,
            Model model,
            Principal principal
    ) {
        AppUser user = authService.findByUsername(principal.getName());
        List<CorporationProfile> profiles = profileRepository.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute("activeProfileId", activeProfile.getId());
        model.addAttribute("profiles", profiles);
        model.addAttribute("createDto", new CreateCorporationDto());
        model.addAttribute("joinDto", new JoinCorporationDto());

        // Get member profile
        CorporationProfile member = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found: " + id));

        // Verify same corporation
        if (!member.getCorporation().getId().equals(activeProfile.getCorporation().getId())) {
            throw new SecurityException("Access denied");
        }

        model.addAttribute("member", member);

        // Get member's departments
        model.addAttribute("memberDepartments", member.getDepartments());

        // Get all departments for assignment
        List<Department> allDepartments = departmentService.getAllDepartments(activeProfile.getCorporation());
        model.addAttribute("allDepartments", allDepartments);

        // Get member's uploaded documents (recent 10)
        List<Document> memberDocuments = documentService.getUserRecentDocuments(member.getUser(), 10);
        model.addAttribute("memberDocuments", memberDocuments);

        return "corporation/members/view";
    }

    /**
     * Add member to department
     */
    @PostMapping("/{id}/add-to-department")
    public String addToDepartment(
            @ActiveUserProfile CorporationProfile activeProfile,
            @PathVariable Long id,
            @RequestParam Long departmentId,
            RedirectAttributes ra
    ) {
        try {
            departmentService.addMember(departmentId, id, activeProfile);
            ra.addFlashAttribute("success", "Member added to department successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to add member: " + e.getMessage());
        }

        return "redirect:/corporation/members/" + id;
    }

    /**
     * Remove member from department
     */
    @PostMapping("/{id}/remove-from-department")
    public String removeFromDepartment(
            @ActiveUserProfile CorporationProfile activeProfile,
            @PathVariable Long id,
            @RequestParam Long departmentId,
            RedirectAttributes ra
    ) {
        try {
            departmentService.removeMember(departmentId, id, activeProfile);
            ra.addFlashAttribute("success", "Member removed from department successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to remove member: " + e.getMessage());
        }

        return "redirect:/corporation/members/" + id;
    }

    /**
     * Update member role
     */
    @PostMapping("/{id}/update-role")
    public String updateRole(
            @ActiveUserProfile CorporationProfile activeProfile,
            @PathVariable Long id,
            @RequestParam String role,
            RedirectAttributes ra
    ) {
        try {
            // Only ADMIN can update roles
            if (!activeProfile.isCorporationAdmin()) {
                throw new SecurityException("Only admins can update member roles");
            }

            CorporationProfile member = profileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            // Verify same corporation
            if (!member.getCorporation().getId().equals(activeProfile.getCorporation().getId())) {
                throw new SecurityException("Access denied");
            }

            member.setRole(hu.martinvass.dms.corporation.domain.CorporationRole.valueOf(role));
            profileRepository.save(member);

            ra.addFlashAttribute("success", "Member role updated successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update role: " + e.getMessage());
        }

        return "redirect:/corporation/members/" + id;
    }
}