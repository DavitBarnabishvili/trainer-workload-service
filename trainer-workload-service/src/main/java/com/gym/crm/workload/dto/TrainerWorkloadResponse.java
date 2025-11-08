package com.gym.crm.workload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trainer workload summary response")
public class TrainerWorkloadResponse {

    @Schema(description = "Trainer's username", example = "trainer.one")
    private String trainerUsername;

    @Schema(description = "Trainer's first name", example = "John")
    private String trainerFirstName;

    @Schema(description = "Trainer's last name", example = "Doe")
    private String trainerLastName;

    @Schema(description = "Trainer's active status", example = "true")
    private Boolean isActive;

    @Schema(description = "List of yearly summaries")
    private List<YearSummaryDto> years;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Yearly training summary")
    public static class YearSummaryDto {

        @Schema(description = "Year", example = "2025")
        private Integer year;

        @Schema(description = "List of monthly summaries for this year")
        private List<MonthSummaryDto> months;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Monthly training summary")
    public static class MonthSummaryDto {

        @Schema(description = "Month number (1-12)", example = "11")
        private Integer month;

        @Schema(description = "Month name", example = "NOVEMBER")
        private String monthName;

        @Schema(description = "Total training duration in minutes for this month", example = "240")
        private Integer totalDuration;
    }
}