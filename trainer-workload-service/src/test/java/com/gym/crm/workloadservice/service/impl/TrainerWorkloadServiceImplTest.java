package com.gym.crm.workloadservice.service.impl;

import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.dto.TrainerWorkloadResponse;
import com.gym.crm.workload.entity.MonthSummary;
import com.gym.crm.workload.entity.TrainerWorkload;
import com.gym.crm.workload.entity.YearSummary;
import com.gym.crm.workload.exception.ValidationException;
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
                .trainingDate(LocalDate.of(2024, 11, 15))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        workload = TrainerWorkload.builder()
                .id("507f1f77bcf86cd799439011")
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .years(new ArrayList<>())
                .build();
    }

    @Test
    void addTraining_ExistingTrainer_ShouldUpdateWorkload() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(workload));
        when(repository.save(any(TrainerWorkload.class))).thenReturn(workload);

        service.addTraining(request);

        verify(repository).findByTrainerUsername("john.doe");
        verify(repository).save(any(TrainerWorkload.class));
        assertEquals(1, workload.getYears().size());
        assertEquals(2024, workload.getYears().getFirst().getYear());
    }

    @Test
    void addTraining_NewTrainer_ShouldCreateWorkload() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.empty());
        when(repository.save(any(TrainerWorkload.class))).thenAnswer(invocation -> {
            TrainerWorkload saved = invocation.getArgument(0);
            saved.setId("507f1f77bcf86cd799439011");
            return saved;
        });

        service.addTraining(request);

        verify(repository).findByTrainerUsername("john.doe");
        verify(repository).save(any(TrainerWorkload.class));
    }

    @Test
    void addTraining_MultipleMonthsInSameYear_ShouldAddToSameYear() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(workload));
        when(repository.save(any(TrainerWorkload.class))).thenReturn(workload);

        service.addTraining(request);

        request.setTrainingDate(LocalDate.of(2024, 12, 10));
        request.setTrainingDuration(90);
        service.addTraining(request);

        assertEquals(1, workload.getYears().size());
        assertEquals(2, workload.getYears().getFirst().getMonths().size());
    }

    @Test
    void addTraining_DifferentYears_ShouldCreateMultipleYears() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(workload));
        when(repository.save(any(TrainerWorkload.class))).thenReturn(workload);

        service.addTraining(request);

        request.setTrainingDate(LocalDate.of(2023, 11, 15));
        service.addTraining(request);

        assertEquals(2, workload.getYears().size());
    }

    @Test
    void addTraining_NullRequest_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> service.addTraining(null));
    }

    @Test
    void addTraining_NullUsername_ShouldThrowValidationException() {
        request.setTrainerUsername(null);
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_EmptyUsername_ShouldThrowValidationException() {
        request.setTrainerUsername("");
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_NullFirstName_ShouldThrowValidationException() {
        request.setTrainerFirstName(null);
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_NullLastName_ShouldThrowValidationException() {
        request.setTrainerLastName(null);
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_NullIsActive_ShouldThrowValidationException() {
        request.setIsActive(null);
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_NullTrainingDate_ShouldThrowValidationException() {
        request.setTrainingDate(null);
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_FutureTrainingDate_ShouldThrowValidationException() {
        request.setTrainingDate(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_NullDuration_ShouldThrowValidationException() {
        request.setTrainingDuration(null);
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_ZeroDuration_ShouldThrowValidationException() {
        request.setTrainingDuration(0);
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_NegativeDuration_ShouldThrowValidationException() {
        request.setTrainingDuration(-10);
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_ExcessiveDuration_ShouldThrowValidationException() {
        request.setTrainingDuration(500);
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_NullActionType_ShouldThrowValidationException() {
        request.setActionType(null);
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_YearBefore1970_ShouldThrowValidationException() {
        request.setTrainingDate(LocalDate.of(1969, 1, 1));
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void addTraining_YearAfter2100_ShouldThrowValidationException() {
        request.setTrainingDate(LocalDate.of(2101, 1, 1));
        assertThrows(ValidationException.class, () -> service.addTraining(request));
    }

    @Test
    void deleteTraining_Success_ShouldReduceDuration() {
        YearSummary year2024 = YearSummary.builder()
                .year(2024)
                .months(new ArrayList<>())
                .build();

        MonthSummary november = MonthSummary.builder()
                .month(11)
                .totalDuration(120)
                .build();

        year2024.getMonths().add(november);
        workload.getYears().add(year2024);

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(workload));
        when(repository.save(any(TrainerWorkload.class))).thenReturn(workload);

        service.deleteTraining(request);

        verify(repository).save(any(TrainerWorkload.class));
        assertEquals(60, november.getTotalDuration());
    }

    @Test
    void deleteTraining_TrainerNotFound_ShouldThrowWorkloadNotFoundException() {
        when(repository.findByTrainerUsername("unknown")).thenReturn(Optional.empty());

        TrainerWorkloadRequest deleteRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("unknown")
                .trainerFirstName("Unknown")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 11, 15))
                .trainingDuration(60)
                .actionType(ActionType.DELETE)
                .build();

        assertThrows(WorkloadNotFoundException.class, () -> service.deleteTraining(deleteRequest));
    }

    @Test
    void deleteTraining_ReducesToZero_ShouldNotGoNegative() {
        request.setTrainingDuration(200);

        YearSummary year = YearSummary.builder()
                .year(2024)
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
    void deleteTraining_NullRequest_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> service.deleteTraining(null));
    }

    @Test
    void getTrainerWorkload_Success_ShouldReturnResponse() {
        YearSummary year = YearSummary.builder()
                .year(2024)
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
        assertEquals("John", response.getTrainerFirstName());
        assertEquals("Doe", response.getTrainerLastName());
        assertEquals(true, response.getIsActive());
        assertEquals(1, response.getYears().size());
        assertEquals(2024, response.getYears().getFirst().getYear());
        assertEquals(1, response.getYears().getFirst().getMonths().size());
        assertEquals(180, response.getYears().getFirst().getMonths().getFirst().getTotalDuration());
    }

    @Test
    void getTrainerWorkload_NotFound_ShouldThrowWorkloadNotFoundException() {
        when(repository.findByTrainerUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(WorkloadNotFoundException.class, () -> service.getTrainerWorkload("unknown"));
    }

    @Test
    void getTrainerWorkload_NullUsername_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> service.getTrainerWorkload(null));
    }

    @Test
    void getTrainerWorkload_EmptyUsername_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> service.getTrainerWorkload(""));
    }

    @Test
    void getTrainerWorkload_WithMultipleYearsAndMonths_ShouldReturnCompleteData() {
        YearSummary year2024 = YearSummary.builder()
                .year(2024)
                .months(new ArrayList<>())
                .build();
        year2024.getMonths().add(MonthSummary.builder().month(1).totalDuration(60).build());
        year2024.getMonths().add(MonthSummary.builder().month(2).totalDuration(90).build());

        YearSummary year2023 = YearSummary.builder()
                .year(2023)
                .months(new ArrayList<>())
                .build();
        year2023.getMonths().add(MonthSummary.builder().month(12).totalDuration(120).build());

        workload.getYears().add(year2024);
        workload.getYears().add(year2023);

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(workload));

        TrainerWorkloadResponse response = service.getTrainerWorkload("john.doe");

        assertEquals(2, response.getYears().size());
        assertEquals(2, response.getYears().get(0).getMonths().size());
        assertEquals(1, response.getYears().get(1).getMonths().size());
    }
}
