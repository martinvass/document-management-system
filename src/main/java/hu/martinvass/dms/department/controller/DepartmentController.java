package hu.martinvass.dms.department.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.department.service.DepartmentService;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.shared.controller.BaseController;
import hu.martinvass.dms.user.repository.AppUserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/corporation/admin/departments")
public class DepartmentController extends BaseController {

    private final DepartmentService departmentService;

    public DepartmentController(AppUserRepository userRepository, CorporationProfileRepository profileRepository, DepartmentService departmentService) {
        super(userRepository, profileRepository);

        this.departmentService = departmentService;
    }

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
        if (!activeProfile.isCorporationAdmin()) {
            return "redirect:/access-denied";
        }

        // Add base attributes
        addBaseAttributes(activeProfile, model, principal);

        var departments = departmentService.getDepartments(
                activeProfile.getCorporation(),
                PageRequest.of(page - 1, 5)
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