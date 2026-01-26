package com.gym.crm.workload.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Month;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthSummary {

    @NotNull
    @Min(1)
    @Max(12)
    private Integer month;

    @NotNull
    @PositiveOrZero
    private Integer totalDuration;

    public String getMonthName() {
        return Month.of(month).name();
    }
}