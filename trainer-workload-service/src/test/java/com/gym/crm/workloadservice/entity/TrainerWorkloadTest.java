package com.gym.crm.workloadservice.entity;

import com.gym.crm.workload.entity.TrainerWorkload;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TrainerWorkloadTest {

    @Test
    void testAddOrUpdateTraining_NewYear() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername("test.trainer")
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .years(new ArrayList<>())
                .build();

        workload.addOrUpdateTraining(2025, 11, 60);

        assertEquals(1, workload.getYears().size());
        assertEquals(2025, workload.getYears().getFirst().getYear());
        assertEquals(1, workload.getYears().getFirst().getMonths().size());
        assertEquals(60, workload.getYears().getFirst().getMonths().getFirst().getTotalDuration());
    }

    @Test
    void testAddOrUpdateTraining_ExistingYear() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername("test.trainer")
                .years(new ArrayList<>())
                .build();

        workload.addOrUpdateTraining(2025, 11, 60);
        workload.addOrUpdateTraining(2025, 11, 30);

        assertEquals(1, workload.getYears().size());
        assertEquals(90, workload.getYears().getFirst().getMonths().getFirst().getTotalDuration());
    }

    @Test
    void testRemoveTraining() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername("test.trainer")
                .years(new ArrayList<>())
                .build();

        workload.addOrUpdateTraining(2025, 11, 100);
        workload.removeTraining(2025, 11, 40);

        assertEquals(60, workload.getYears().getFirst().getMonths().getFirst().getTotalDuration());
    }

    @Test
    void testRemoveTraining_MoreThanExists() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername("test.trainer")
                .years(new ArrayList<>())
                .build();

        workload.addOrUpdateTraining(2025, 11, 50);
        workload.removeTraining(2025, 11, 100);

        assertEquals(0, workload.getYears().getFirst().getMonths().getFirst().getTotalDuration());
    }
}