package com.gym.crm.workloadservice.messaging;

import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.exception.ValidationException;
import com.gym.crm.workload.messaging.WorkloadMessageListener;
import com.gym.crm.workload.service.TrainerWorkloadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageListenerTest {

    @Mock
    private TrainerWorkloadService workloadService;

    @Mock
    private Validator validator;

    private WorkloadMessageListener listener;

    @BeforeEach
    void setUp() {
        listener = new WorkloadMessageListener(workloadService, validator);
    }

    @Test
    void receiveWorkloadUpdate_WithAddAction_ShouldCallAddTraining() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 11, 15))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        when(validator.validate(request)).thenReturn(Collections.emptySet());

        listener.receiveWorkloadUpdate(request);

        verify(workloadService).addTraining(request);
        verify(workloadService, never()).deleteTraining(any());
    }

    @Test
    void receiveWorkloadUpdate_WithDeleteAction_ShouldCallDeleteTraining() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 11, 15))
                .trainingDuration(60)
                .actionType(ActionType.DELETE)
                .build();

        when(validator.validate(request)).thenReturn(Collections.emptySet());

        listener.receiveWorkloadUpdate(request);

        verify(workloadService).deleteTraining(request);
        verify(workloadService, never()).addTraining(any());
    }

    @Test
    void receiveWorkloadUpdate_WithTransactionId_ShouldUseProvidedTransactionId() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 11, 15))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .transactionId("existing-tx-123")
                .build();

        when(validator.validate(request)).thenReturn(Collections.emptySet());

        listener.receiveWorkloadUpdate(request);

        verify(workloadService).addTraining(request);
    }

    @Test
    void receiveWorkloadUpdate_WithNullRequest_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> listener.receiveWorkloadUpdate(null));

        verify(workloadService, never()).addTraining(any());
        verify(workloadService, never()).deleteTraining(any());
    }

    @Test
    void receiveWorkloadUpdate_WithValidationErrors_ShouldThrowValidationException() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .actionType(ActionType.ADD)
                .build();

        Set<ConstraintViolation<TrainerWorkloadRequest>> violations = new HashSet<>();
        ConstraintViolation<TrainerWorkloadRequest> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getMessage()).thenReturn("must not be null");
        violations.add(violation);

        when(validator.validate(request)).thenReturn(violations);

        assertThrows(ValidationException.class, () -> listener.receiveWorkloadUpdate(request));

        verify(workloadService, never()).addTraining(any());
        verify(workloadService, never()).deleteTraining(any());
    }

    @Test
    void receiveWorkloadUpdate_WhenServiceThrowsException_ShouldRethrowException() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 11, 15))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        when(validator.validate(request)).thenReturn(Collections.emptySet());
        doThrow(new RuntimeException("Database error")).when(workloadService).addTraining(request);

        assertThrows(RuntimeException.class, () -> listener.receiveWorkloadUpdate(request));

        verify(workloadService).addTraining(request);
    }

    @Test
    void receiveWorkloadUpdate_WithInvalidActionType_ShouldThrowValidationException() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 11, 15))
                .trainingDuration(60)
                .actionType(null)
                .build();

        when(validator.validate(request)).thenReturn(Collections.emptySet());

        assertThrows(ValidationException.class, () -> listener.receiveWorkloadUpdate(request));

        verify(workloadService, never()).addTraining(any());
        verify(workloadService, never()).deleteTraining(any());
    }

    @Test
    void receiveWorkloadUpdate_WithAddAction_ShouldNotCallDelete() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 11, 15))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        when(validator.validate(request)).thenReturn(Collections.emptySet());

        listener.receiveWorkloadUpdate(request);

        verify(workloadService).addTraining(request);
        verifyNoMoreInteractions(workloadService);
    }

    @Test
    void receiveWorkloadUpdate_WithDeleteAction_ShouldNotCallAdd() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 11, 15))
                .trainingDuration(60)
                .actionType(ActionType.DELETE)
                .build();

        when(validator.validate(request)).thenReturn(Collections.emptySet());

        listener.receiveWorkloadUpdate(request);

        verify(workloadService).deleteTraining(request);
        verifyNoMoreInteractions(workloadService);
    }
}
