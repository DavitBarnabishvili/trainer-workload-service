package com.gym.crm.workload.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainer_workload")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String trainerUsername;

    @Column(nullable = false)
    private String trainerFirstName;

    @Column(nullable = false)
    private String trainerLastName;

    @Column(nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "trainerWorkload", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private List<YearSummary> years = new ArrayList<>();

    public void addOrUpdateTraining(int year, int month, int duration) {
        YearSummary yearSummary = years.stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYear = YearSummary.builder()
                            .year(year)
                            .trainerWorkload(this)
                            .months(new ArrayList<>())
                            .build();
                    years.add(newYear);
                    return newYear;
                });

        yearSummary.addOrUpdateMonth(month, duration);
    }

    public void removeTraining(int year, int month, int duration) {
        years.stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .ifPresent(yearSummary -> yearSummary.removeFromMonth(month, duration));
    }
}