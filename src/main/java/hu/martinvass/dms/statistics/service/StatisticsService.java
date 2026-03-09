package hu.martinvass.dms.statistics.service;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.department.repository.DepartmentRepository;
import hu.martinvass.dms.document.domain.DocumentStatus;
import hu.martinvass.dms.document.repository.DocumentRepository;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.statistics.dto.StatisticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final DocumentRepository documentRepository;
    private final CorporationProfileRepository profileRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Get document upload trends for last N days
     */
    public List<StatisticsDto.DocumentTrendDto> getDocumentUploadTrends(Corporation corporation, int days) {
        var fromDate = LocalDateTime.now().minusDays(days);

        var results = documentRepository.getUploadTrendsByDays(
                corporation.getId(),
                fromDate
        );

        return results.stream()
                .map(row -> new StatisticsDto.DocumentTrendDto(
                        convertToLocalDate(row[0]),
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get file type distribution
     */
    public List<StatisticsDto.FileTypeDto> getFileTypeDistribution(Corporation corporation) {
        var results = documentRepository.getFileTypeDistribution(corporation.getId());

        return results.stream()
                .map(row -> new StatisticsDto.FileTypeDto(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        row[2] != null ? ((Number) row[2]).longValue() : 0L
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get top document uploaders
     */
    public List<StatisticsDto.TopUploaderDto> getTopUploaders(Corporation corporation, int limit) {
        var results = documentRepository.getTopUploaders(
                corporation.getId(),
                limit
        );

        return results.stream()
                .map(row -> new StatisticsDto.TopUploaderDto(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get documents by department
     */
    public List<StatisticsDto.DocumentsByDepartmentDto> getDocumentsByDepartment(Corporation corporation) {
        var results = documentRepository
                .getDocumentsByDepartment(corporation.getId());

        return results.stream()
                .map(row -> new StatisticsDto.DocumentsByDepartmentDto(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get team growth for last N days
     */
    public List<StatisticsDto.TeamGrowthDto> getTeamGrowth(Corporation corporation, int days) {
        var fromDate = LocalDateTime.now().minusDays(days);

        // Get baseline: members BEFORE the time window
        var baselineCount = profileRepository.countProfilesBeforeDate(
                corporation.getId(),
                fromDate
        );

        // Get new members within the time window
        List<Object[]> results = profileRepository.getProfilesForGrowth(
                corporation.getId(),
                fromDate
        );

        // Group by date
        var countByDate = results.stream()
                .collect(Collectors.groupingBy(
                        row -> {
                            Object dateObj = row[0];
                            if (dateObj instanceof LocalDateTime) {
                                return ((LocalDateTime) dateObj).toLocalDate();
                            }
                            return LocalDate.parse(dateObj.toString().substring(0, 10));
                        },
                        TreeMap::new,
                        Collectors.counting()
                ));

        // Calculate cumulative starting from baseline
        var cumulative = baselineCount != null ? baselineCount : 0L;
        List<StatisticsDto.TeamGrowthDto> growth = new ArrayList<>();

        for (var entry : countByDate.entrySet()) {
            cumulative += entry.getValue();
            growth.add(new StatisticsDto.TeamGrowthDto(
                    entry.getKey(),
                    entry.getValue(),
                    cumulative
            ));
        }

        return growth;
    }

    /**
     * Get department member distribution
     */
    public List<StatisticsDto.DepartmentMembersDto> getDepartmentMemberDistribution(Corporation corporation) {
        var results = departmentRepository
                .getDepartmentMemberDistribution(corporation.getId());

        return results.stream()
                .map(row -> new StatisticsDto.DepartmentMembersDto(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get overview statistics
     */
    public OverviewStatsDto getOverviewStats(Corporation corporation) {
        return new OverviewStatsDto(
                documentRepository.countByCorporationAndStatus(corporation, DocumentStatus.ACTIVE),
                profileRepository.countByCorporation(corporation),
                departmentRepository.countByCorporation(corporation)
        );
    }

    private LocalDate convertToLocalDate(Object dateObj) {
        if (dateObj == null) {
            return LocalDate.now();
        }

        if (dateObj instanceof LocalDateTime) {
            return ((LocalDateTime) dateObj).toLocalDate();
        }
        
        if (dateObj instanceof Date) {
            return ((Date) dateObj).toLocalDate();
        }

        if (dateObj instanceof java.util.Date) {
            return new Date(((java.util.Date) dateObj).getTime()).toLocalDate();
        }

        if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        }

        try {
            return LocalDate.parse(dateObj.toString());
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class OverviewStatsDto {
        private Long totalDocuments;
        private Long totalMembers;
        private Long totalDepartments;
    }
}