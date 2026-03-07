package hu.martinvass.dms.user.controller;

import hu.martinvass.dms.activity.service.ActivityService;
import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.corporation.dto.CreateCorporationDto;
import hu.martinvass.dms.corporation.dto.JoinCorporationDto;
import hu.martinvass.dms.department.service.DepartmentService;
import hu.martinvass.dms.document.service.DocumentService;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.shared.controller.BaseController;
import hu.martinvass.dms.shared.utils.FileUtils;
import hu.martinvass.dms.user.repository.AppUserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController extends BaseController {

    private final DocumentService documentService;
    private final ActivityService activityService;
    private final DepartmentService departmentService;

    private final CorporationProfileRepository corporationProfileRepository;

    public HomeController(AppUserRepository userRepository, CorporationProfileRepository profileRepository, DocumentService documentService, ActivityService activityService, DepartmentService departmentService, CorporationProfileRepository corporationProfileRepository) {
        super(userRepository, profileRepository);

        this.documentService = documentService;
        this.activityService = activityService;
        this.departmentService = departmentService;
        this.corporationProfileRepository = corporationProfileRepository;
    }

    @GetMapping("/home")
    public String home(
            @ActiveUserProfile CorporationProfile activeProfile,
            Model model,
            Principal principal
    ) {
        // Add base attributes
        addBaseAttributes(activeProfile, model, principal);

        if (activeProfile != null && activeProfile.getCorporation() != null) {
            var totalDocuments = documentService.getTotalDocuments(activeProfile);
            var documentsToMigrate = documentService.getDocumentsToMigrate(activeProfile);
            var activeUsers = corporationProfileRepository.countByCorporation(activeProfile.getCorporation());
            var departmentCount = departmentService.countDepartments(activeProfile.getCorporation());

            model.addAttribute("totalDocuments", totalDocuments);
            model.addAttribute("documentsToMigrate", documentsToMigrate);
            model.addAttribute("activeUsers", activeUsers);
            model.addAttribute("departmentCount", departmentCount);

            var recentDocuments = documentService.getRecentDocuments(activeProfile, 4);
            model.addAttribute("recentDocuments", recentDocuments);

            var recentActivities = activityService.getRecentActivities(
                    activeProfile.getCorporation(),
                    4
            );
            model.addAttribute("recentActivities", recentActivities);

            var totalStorage = documentService.getTotalStorageUsed(activeProfile);
            var totalStorageReadable = FileUtils.humanReadableSize(totalStorage);
            model.addAttribute("totalStorage", totalStorage);
            model.addAttribute("totalStorageReadable", totalStorageReadable);
        }

        model.addAttribute("createDto", new CreateCorporationDto());
        model.addAttribute("joinDto", new JoinCorporationDto());

        return "home";
    }
}