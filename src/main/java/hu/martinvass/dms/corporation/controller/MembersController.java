package hu.martinvass.dms.corporation.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.corporation.domain.CorporationRole;
import hu.martinvass.dms.department.service.DepartmentService;
import hu.martinvass.dms.document.service.DocumentService;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.profile.service.CorporationProfileService;
import hu.martinvass.dms.shared.controller.BaseController;
import hu.martinvass.dms.user.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/corporation/members")
public class MembersController extends BaseController {

    private final CorporationProfileRepository profileRepository;
    private final CorporationProfileService profileService;
    private final DepartmentService departmentService;
    private final DocumentService documentService;

    public MembersController(AppUserRepository userRepository, CorporationProfileRepository profileRepository, CorporationProfileRepository profileRepository1, CorporationProfileService profileService, DepartmentService departmentService, DocumentService documentService) {
        super(userRepository, profileRepository);

        this.profileRepository = profileRepository1;
        this.profileService = profileService;
        this.departmentService = departmentService;
        this.documentService = documentService;
    }

    /**
     * List all members
     */
    @GetMapping
    public String list(
            @ActiveUserProfile CorporationProfile activeProfile,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long departmentId,
            Model model,
            Principal principal
    ) {
        Page<CorporationProfile> members;

        if (role != null || departmentId != null) {
            members = profileService.findFiltered(
                    activeProfile.getCorporation(),
                    role,
                    departmentId,
                    PageRequest.of(page - 1, size)
            );
        } else {
            members = profileRepository.findByCorporation(
                    activeProfile.getCorporation(),
                    PageRequest.of(page - 1, size)
            );
        }

        var adminsCount = profileRepository.countByCorporationAndRole(
                activeProfile.getCorporation(),
                CorporationRole.ADMIN
        );
        var employeesCount = profileRepository.countByCorporationAndRole(
                activeProfile.getCorporation(),
                CorporationRole.EMPLOYEE
        );

        var startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        var joinedThisMonthCount = profileRepository.countByCorporationAndCreatedAfter(
                activeProfile.getCorporation(),
                startOfMonth
        );

        var allDepartments = departmentService.getAllDepartments(activeProfile.getCorporation());

        // Add basic attributes
        addBaseAttributes(activeProfile, model, principal);

        model.addAttribute("members", members);
        model.addAttribute("departments", allDepartments);
        model.addAttribute("currentPage", page);
        model.addAttribute("adminsCount", adminsCount);
        model.addAttribute("employeesCount", employeesCount);
        model.addAttribute("joinedThisMonthCount", joinedThisMonthCount);

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
        // Add base attributes
        addBaseAttributes(activeProfile, model, principal);

        var member = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found: " + id));

        if (!member.getCorporation().getId().equals(activeProfile.getCorporation().getId())) {
            throw new SecurityException("Access denied");
        }

        model.addAttribute("member", member);
        model.addAttribute("memberDepartments", member.getDepartments());

        var allDepartments = departmentService.getAllDepartments(activeProfile.getCorporation());
        model.addAttribute("allDepartments", allDepartments);

        var memberDocuments = documentService.getUserRecentDocuments(member.getUser(), 10);
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
            if (!activeProfile.isCorporationAdmin()) {
                throw new SecurityException("Only admins can update member roles");
            }

            var member = profileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Member not found"));

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