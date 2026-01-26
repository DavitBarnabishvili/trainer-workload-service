package com.gym.crm.workloadservice.service.impl;

import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.dto.TrainerWorkloadResponse;
import com.gym.crm.workload.entity.MonthSummary;
import com.gym.crm.workload.entity.TrainerWorkload;
import com.gym.crm.workload.entity.YearSummary;
import com.gym.crm.workload.exception.WorkloadNotFoundException;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import com.gym.crm.workload.service.impl.TrainerWorkloadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerWorkloadServiceImplTest {

    @Mock
    private TrainerWorkloadRepository repository;

    @InjectMocks
    private TrainerWorkloadServiceImpl service;

    private TrainerWorkloadRequest request;
    private TrainerWorkload workload;

    @BeforeEach
    void setUp() {
        request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2025, 11, 15))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        workload = TrainerWorkload.builder()
                .id(1L)
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .years(new ArrayList<>())
                .build();
    }

    @Test
    void testAddTraining_ExistingTrainer() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(workload));
        when(repository.save(any(TrainerWorkload.class))).thenReturn(workload);

        service.addTraining(request);

        verify(repository).findByTrainerUsername("john.doe");
        verify(repository).save(any(TrainerWorkload.class));

        assertEquals(1, workload.getYears().size());
        assertEquals(2025, workload.getYears().getFirst().getYear());
    }

    @Test
    void testAddTraining_NewTrainer() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.empty());
        when(repository.save(any(TrainerWorkload.class))).thenAnswer(invocation -> {
            TrainerWorkload saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        service.addTraining(request);

        verify(repository).findByTrainerUsername("john.doe");
        verify(repository).save(any(TrainerWorkload.class));
    }

    @Test
    void testAddTraining_MultipleMonthsInYear() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(workload));
        when(repository.save(any(TrainerWorkload.class))).thenReturn(workload);

        service.addTraining(request);

        request.setTrainingDate(LocalDate.of(2025, 12, 10));
        request.setTrainingDuration(90);
        service.addTraining(request);

        assertEquals(1, workload.getYears().size());
        assertEquals(2, workload.getYears().getFirst().getMonths().size());
    }

    @Test
    void testDeleteTraining_Success() {
        YearSummary year2025 = YearSummary.builder()
                .year(2025)
                .months(new ArrayList<>())
                .build();

        MonthSummary november = MonthSummary.builder()
                .month(11)
                .totalDuration(120)
                .build();

        year2025.getMonths().add(november);
        workload.getYears().add(year2025);

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(workload));
        when(repository.save(any(TrainerWorkload.class))).thenReturn(workload);

        service.deleteTraining(request);

        verify(repository).save(any(TrainerWorkload.class));
        assertEquals(60, november.getTotalDuration());
    }

    @Test
    void testDeleteTraining_TrainerNotFound() {
        when(repository.findByTrainerUsername("unknown")).thenReturn(Optional.empty());

        TrainerWorkloadRequest deleteRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("unknown")
                .trainerFirstName("Unknown")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(LocalDate.of(2025, 11, 15))
                .trainingDuration(60)
                .actionType(ActionType.DELETE)
                .build();

        assertThrows(WorkloadNotFoundException.class, () -> service.deleteTraining(deleteRequest));
    }

    @Test
    void testDeleteTraining_ReducesToZero() {
        request.setTrainingDuration(200);

        YearSummary year = YearSummary.builder()
                .year(2025)
                .months(new ArrayList<>())
                .build();

        MonthSummary month = MonthSummary.builder()
                .month(11)
                .totalDuration(100)
                .build();

        year.getMonths().add(month);
        workload.getYears().add(year);

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(workload));
        when(repository.save(any(TrainerWorkload.class))).thenReturn(workload);

        service.deleteTraining(request);

        assertEquals(0, month.getTotalDuration());
    }

    @Test
    void testGetTrainerWorkload_Success() {
        YearSummary year = YearSummary.builder()
                .year(2025)
                .months(Collections.singletonList(
                        MonthSummary.builder()
                                .month(11)
                                .totalDuration(180)
                                .build()
                ))
                .build();

        workload.getYears().add(year);

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(workload));

        TrainerWorkloadResponse response = service.getTrainerWorkload("john.doe");

        assertNotNull(response);
        assertEquals("john.doe", response.getTrainerUsername());
        assertEquals(1, response.getYears().size());
        assertEquals(2025, response.getYears().getFirst().getYear());
        assertEquals(1, response.getYears().getFirst().getMonths().size());
        assertEquals(180, response.getYears().getFirst().getMonths().getFirst().getTotalDuration());
    }

    @Test
    void testGetTrainerWorkload_NotFound() {
        when(repository.findByTrainerUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(WorkloadNotFoundException.class, () -> service.getTrainerWorkload("unknown"));
    }
}
