package com.gym.crm.workload.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Month;

@Entity
@Table(name = "month_summary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "month_num", nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer totalDuration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_summary_id", nullable = false)
    @ToString.Exclude
    private YearSummary yearSummary;

    public String getMonthName() {
        return Month.of(month).name();
    }
}