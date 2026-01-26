package com.gym.crm.workload.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_workload")
@CompoundIndex(name = "trainer_name_idx", def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {

    @Id
    private String id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Indexed(unique = true)
    private String trainerUsername;

    @NotBlank
    @Size(min = 1, max = 50)
    @Indexed
    private String trainerFirstName;

    @NotBlank
    @Size(min = 1, max = 50)
    @Indexed
    private String trainerLastName;

    @NotNull
    private Boolean isActive;

    @Builder.Default
    private List<YearSummary> years = new ArrayList<>();

    public void addOrUpdateTraining(int year, int month, int duration) {
        YearSummary yearSummary = years.stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYear = YearSummary.builder()
                            .year(year)
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