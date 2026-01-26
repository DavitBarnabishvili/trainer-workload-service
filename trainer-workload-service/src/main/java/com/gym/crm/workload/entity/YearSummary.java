package com.gym.crm.workload.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearSummary {

    @NotNull
    @Min(1970)
    @Max(2100)
    private Integer year;

    @Builder.Default
    private List<MonthSummary> months = new ArrayList<>();

    public void addOrUpdateMonth(int month, int duration) {
        MonthSummary monthSummary = months.stream()
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .orElseGet(() -> {
                    MonthSummary newMonth = MonthSummary.builder()
                            .month(month)
                            .totalDuration(0)
                            .build();
                    months.add(newMonth);
                    return newMonth;
                });

        monthSummary.setTotalDuration(monthSummary.getTotalDuration() + duration);
    }

    public void removeFromMonth(int month, int duration) {
        months.stream()
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .ifPresent(monthSummary -> {
                    int newDuration = Math.max(0, monthSummary.getTotalDuration() - duration);
                    monthSummary.setTotalDuration(newDuration);
                });
    }
}