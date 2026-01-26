package com.gym.crm.workload.messaging;

import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class WorkloadMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadMessageListener.class);

    private final TrainerWorkloadService workloadService;

    public WorkloadMessageListener(TrainerWorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @JmsListener(destination = "${activemq.queues.trainer-workload}")
    public void receiveWorkloadUpdate(TrainerWorkloadRequest request) {
        logger.info("Received workload message for trainer: {}", request.getTrainerUsername());

        try {
            if (request.getActionType() == ActionType.ADD) {
                workloadService.addTraining(request);
            } else if (request.getActionType() == ActionType.DELETE) {
                workloadService.deleteTraining(request);
            }
            logger.info("Successfully processed workload update for trainer: {}", request.getTrainerUsername());
        } catch (Exception e) {
            logger.error("Failed to process workload update for trainer: {}",
                    request.getTrainerUsername(), e);
            throw e;
        }
    }
}
