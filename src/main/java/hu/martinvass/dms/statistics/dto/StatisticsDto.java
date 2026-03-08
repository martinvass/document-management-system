package hu.martinvass.dms.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class StatisticsDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentTrendDto {
        private LocalDate date;
        private Long count;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileTypeDto {
        private String type;
        private Long count;
        private Long totalSize;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopUploaderDto {
        private String username;
        private Long uploadCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentsByDepartmentDto {
        private String departmentName;
        private Long documentCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeamGrowthDto {
        private LocalDate date;
        private Long newMembers;
        private Long cumulativeMembers;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DepartmentMembersDto {
        private String departmentName;
        private Long memberCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChartDataDto {
        private List<String> labels;
        private List<Number> data;
        private String label;
    }
}