package com.gym.crm.workload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadResponse {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean isActive;
    private List<YearSummaryDto> years;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearSummaryDto {
        private Integer year;
        private List<MonthSummaryDto> months;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthSummaryDto {
        private Integer month;
        private String monthName;
        private Integer totalDuration;
    }
}