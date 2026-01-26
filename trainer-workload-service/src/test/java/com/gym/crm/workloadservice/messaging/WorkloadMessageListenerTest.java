package com.gym.crm.workloadservice.messaging;

import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.messaging.WorkloadMessageListener;
import com.gym.crm.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageListenerTest {

    @Mock
    private TrainerWorkloadService workloadService;

    private WorkloadMessageListener listener;

    @BeforeEach
    void setUp() {
        listener = new WorkloadMessageListener(workloadService);
    }

    @Test
    void receiveWorkloadUpdate_ShouldCallAddTraining_WhenActionTypeIsADD() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .actionType(ActionType.ADD)
                .build();

        listener.receiveWorkloadUpdate(request);

        verify(workloadService).addTraining(request);
    }

    @Test
    void receiveWorkloadUpdate_ShouldCallDeleteTraining_WhenActionTypeIsDELETE() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .actionType(ActionType.DELETE)
                .build();

        listener.receiveWorkloadUpdate(request);

        verify(workloadService).deleteTraining(request);
    }

    @Test
    void receiveWorkloadUpdate_ShouldRethrowException_WhenServiceThrowsException() {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .actionType(ActionType.ADD)
                .build();

        doThrow(new RuntimeException("Database error")).when(workloadService).addTraining(request);

        assertThrows(RuntimeException.class, () -> listener.receiveWorkloadUpdate(request));

        verify(workloadService).addTraining(request);
    }
}
