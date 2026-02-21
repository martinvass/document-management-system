package hu.martinvass.dms.user.controller;

import hu.martinvass.dms.activity.ActivityLog;
import hu.martinvass.dms.activity.ActivityService;
import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.auth.AuthService;
import hu.martinvass.dms.corporation.document.domain.Document;
import hu.martinvass.dms.corporation.document.service.DocumentService;
import hu.martinvass.dms.department.service.DepartmentService;
import hu.martinvass.dms.dto.CreateCorporationDto;
import hu.martinvass.dms.dto.JoinCorporationDto;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.user.AppUser;
import hu.martinvass.dms.utils.FileUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
@AllArgsConstructor
public class HomeController {

    private final AuthService authService;
    private final DocumentService documentService;
    private final ActivityService activityService;
    private final DepartmentService departmentService;

    private final CorporationProfileRepository corporationProfileRepository;

    @GetMapping("/home")
    public String home(
            @ActiveUserProfile CorporationProfile activeProfile,
            Model model,
            Principal principal
    ) {
        AppUser user = authService.findByUsername(principal.getName());
        List<CorporationProfile> profiles = corporationProfileRepository.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute("activeProfileId", activeProfile != null ? activeProfile.getId() : -1);
        model.addAttribute("profiles", profiles);

        // Modal DTOs
        model.addAttribute("createDto", new CreateCorporationDto());
        model.addAttribute("joinDto", new JoinCorporationDto());

        // Dashboard statistics and data (only if user has active profile)
        if (activeProfile != null && activeProfile.getCorporation() != null) {
            // Statistics
            long totalDocuments = documentService.getTotalDocuments(activeProfile);
            long documentsToMigrate = documentService.getDocumentsToMigrate(activeProfile);
            long activeUsers = corporationProfileRepository.countByCorporation(activeProfile.getCorporation());
            long departmentCount = departmentService.countDepartments(activeProfile.getCorporation());

            model.addAttribute("totalDocuments", totalDocuments);
            model.addAttribute("documentsToMigrate", documentsToMigrate);
            model.addAttribute("activeUsers", activeUsers);
            model.addAttribute("departmentCount", departmentCount);

            // Recent documents (4 most recent)
            List<Document> recentDocuments = documentService.getRecentDocuments(activeProfile, 4);
            model.addAttribute("recentDocuments", recentDocuments);

            List<ActivityLog> recentActivities = activityService.getRecentActivities(
                    activeProfile.getCorporation(),
                    4
            );
            model.addAttribute("recentActivities", recentActivities);

            // Storage statistics
            long totalStorage = documentService.getTotalStorageUsed(activeProfile);
            String totalStorageReadable = FileUtils.humanReadableSize(totalStorage);
            model.addAttribute("totalStorage", totalStorage);
            model.addAttribute("totalStorageReadable", totalStorageReadable);
        }

        return "home";
    }
}