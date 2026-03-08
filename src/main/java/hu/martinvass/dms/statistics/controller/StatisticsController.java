package hu.martinvass.dms.statistics.controller;

import hu.martinvass.dms.annotations.ActiveUserProfile;
import hu.martinvass.dms.annotations.RequireCorpAdmin;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.shared.controller.BaseController;
import hu.martinvass.dms.statistics.dto.StatisticsDto;
import hu.martinvass.dms.statistics.service.StatisticsService;
import hu.martinvass.dms.user.repository.AppUserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class StatisticsController extends BaseController {

    private final StatisticsService statisticsService;

    public StatisticsController(AppUserRepository userRepository, CorporationProfileRepository profileRepository, StatisticsService statisticsService) {
        super(userRepository, profileRepository);

        this.statisticsService = statisticsService;
    }

    /**
     * Show statistics page
     */
    @GetMapping("/corporation/admin/statistics")
    @RequireCorpAdmin
    public String statistics(
            @ActiveUserProfile CorporationProfile activeProfile,
            Model model,
            Principal principal
    ) {
        var corporation = activeProfile.getCorporation();

        addBaseAttributes(activeProfile, model, principal);

        var overview = statisticsService.getOverviewStats(corporation);
        model.addAttribute("totalDocuments", overview.getTotalDocuments());
        model.addAttribute("totalMembers", overview.getTotalMembers());
        model.addAttribute("totalDepartments", overview.getTotalDepartments());

        return "corporation/admin/statistics";
    }

    /**
     * Document upload trends
     */
    @GetMapping("/statistics/api/document-trends")
    @ResponseBody
    public StatisticsDto.ChartDataDto getDocumentTrends(@ActiveUserProfile CorporationProfile activeProfile) {
        var corp = activeProfile.getCorporation();
        var trends = statisticsService.getDocumentUploadTrends(corp, 30);

        return new StatisticsDto.ChartDataDto(
                trends.stream().map(t -> t.getDate().toString()).collect(Collectors.toList()),
                trends.stream().map(StatisticsDto.DocumentTrendDto::getCount).collect(Collectors.toList()),
                "Documents Uploaded"
        );
    }

    /**
     * File type distribution
     */
    @GetMapping("/statistics/api/file-types")
    @ResponseBody
    public Map<String, Object> getFileTypes(@ActiveUserProfile CorporationProfile activeProfile) {
        var corp = activeProfile.getCorporation();
        var types = statisticsService.getFileTypeDistribution(corp);

        Map<String, Object> result = new HashMap<>();
        result.put("labels", types.stream().map(StatisticsDto.FileTypeDto::getType).collect(Collectors.toList()));
        result.put("data", types.stream().map(StatisticsDto.FileTypeDto::getCount).collect(Collectors.toList()));

        return result;
    }

    /**
     * Top uploaders
     */
    @GetMapping("/statistics/api/top-uploaders")
    @ResponseBody
    public StatisticsDto.ChartDataDto getTopUploaders(@ActiveUserProfile CorporationProfile activeProfile) {
        var corp = activeProfile.getCorporation();
        var uploaders = statisticsService.getTopUploaders(corp, 10);

        return new StatisticsDto.ChartDataDto(
                uploaders.stream().map(StatisticsDto.TopUploaderDto::getUsername).collect(Collectors.toList()),
                uploaders.stream().map(StatisticsDto.TopUploaderDto::getUploadCount).collect(Collectors.toList()),
                "Documents Uploaded"
        );
    }

    /**
     * Documents by department
     */
    @GetMapping("/statistics/api/documents-by-department")
    @ResponseBody
    public StatisticsDto.ChartDataDto getDocumentsByDepartment(@ActiveUserProfile CorporationProfile activeProfile) {
        var corp = activeProfile.getCorporation();
        var docs = statisticsService.getDocumentsByDepartment(corp);

        return new StatisticsDto.ChartDataDto(
                docs.stream().map(StatisticsDto.DocumentsByDepartmentDto::getDepartmentName).collect(Collectors.toList()),
                docs.stream().map(StatisticsDto.DocumentsByDepartmentDto::getDocumentCount).collect(Collectors.toList()),
                "Documents"
        );
    }

    /**
     * Team growth
     */
    @GetMapping("/statistics/api/team-growth")
    @ResponseBody
    public StatisticsDto.ChartDataDto getTeamGrowth(@ActiveUserProfile CorporationProfile activeProfile) {
        var corp = activeProfile.getCorporation();
        var growth = statisticsService.getTeamGrowth(corp, 30);

        return new StatisticsDto.ChartDataDto(
                growth.stream().map(t -> t.getDate().toString()).collect(Collectors.toList()),
                growth.stream().map(StatisticsDto.TeamGrowthDto::getCumulativeMembers).collect(Collectors.toList()),
                "Total Team Members"
        );
    }

    /**
     * Department member distribution
     */
    @GetMapping("/statistics/api/department-members")
    @ResponseBody
    public StatisticsDto.ChartDataDto getDepartmentMembers(@ActiveUserProfile CorporationProfile activeProfile) {
        var corp = activeProfile.getCorporation();
        var members = statisticsService.getDepartmentMemberDistribution(corp);

        return new StatisticsDto.ChartDataDto(
                members.stream().map(StatisticsDto.DepartmentMembersDto::getDepartmentName).collect(Collectors.toList()),
                members.stream().map(StatisticsDto.DepartmentMembersDto::getMemberCount).collect(Collectors.toList()),
                "Members"
        );
    }
}