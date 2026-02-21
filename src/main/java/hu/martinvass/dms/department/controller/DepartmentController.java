package hu.martinvass.dms.department.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.auth.AuthService;
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
@RequestMapping("/corporation/admin/departments")
@AllArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final CorporationProfileRepository profileRepository;
    private final AuthService authService;

    /**
     * List all departments
     */
    @GetMapping
    public String list(
            @ActiveUserProfile CorporationProfile activeProfile,
            @RequestParam(defaultValue = "1") int page,
            Model model,
            Principal principal
    ) {
        // Only ADMIN can manage departments
        if (!activeProfile.isCorporationAdmin()) {
            return "redirect:/access-denied";
        }

        AppUser user = authService.findByUsername(principal.getName());
        List<CorporationProfile> profiles = profileRepository.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute("activeProfileId", activeProfile.getId());
        model.addAttribute("profiles", profiles);
        model.addAttribute("createDto", new CreateCorporationDto());
        model.addAttribute("joinDto", new JoinCorporationDto());

        // Get departments (paginated)
        Page<Department> departments = departmentService.getDepartments(
                activeProfile.getCorporation(),
                PageRequest.of(page - 1, 15)
        );

        model.addAttribute("departments", departments);
        model.addAttribute("currentPage", page);

        return "corporation/admin/departments";
    }

    /**
     * Create department
     */
    @PostMapping("/create")
    public String create(
            @ActiveUserProfile CorporationProfile activeProfile,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            RedirectAttributes ra
    ) {
        try {
            if (!activeProfile.isCorporationAdmin()) {
                throw new SecurityException("Only admins can create departments");
            }

            departmentService.createDepartment(
                    activeProfile.getCorporation(),
                    name,
                    description,
                    activeProfile.getUser()
            );

            ra.addFlashAttribute("success", "Department created successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to create department: " + e.getMessage());
        }

        return "redirect:/corporation/admin/departments";
    }

    /**
     * Update department
     */
    @PostMapping("/{id}/update")
    public String update(
            @ActiveUserProfile CorporationProfile activeProfile,
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            RedirectAttributes ra
    ) {
        try {
            if (!activeProfile.isCorporationAdmin()) {
                throw new SecurityException("Only admins can update departments");
            }

            departmentService.updateDepartment(
                    id,
                    activeProfile.getCorporation(),
                    name,
                    description
            );

            ra.addFlashAttribute("success", "Department updated successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update department: " + e.getMessage());
        }

        return "redirect:/corporation/admin/departments";
    }
}