package com.gym.crm.workload.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "year_summary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_num", nullable = false)
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_workload_id", nullable = false)
    @ToString.Exclude
    private TrainerWorkload trainerWorkload;

    @OneToMany(mappedBy = "yearSummary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
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
                            .yearSummary(this)
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