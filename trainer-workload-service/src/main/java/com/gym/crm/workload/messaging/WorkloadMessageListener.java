package com.gym.crm.workload.messaging;

import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.exception.ValidationException;
import com.gym.crm.workload.service.TrainerWorkloadService;
import com.gym.crm.workload.util.TransactionContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class WorkloadMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadMessageListener.class);

    private final TrainerWorkloadService workloadService;
    private final Validator validator;

    public WorkloadMessageListener(TrainerWorkloadService workloadService, Validator validator) {
        this.workloadService = workloadService;
        this.validator = validator;
    }

    @JmsListener(destination = "${activemq.queues.trainer-workload}")
    public void receiveWorkloadUpdate(TrainerWorkloadRequest request) {
        String transactionId = null;

        try {
            if (request != null && request.getTransactionId() != null) {
                transactionId = TransactionContext.startTransaction(request.getTransactionId());
            } else {
                transactionId = TransactionContext.startTransaction();
            }

            logger.info("[TRANSACTION-START] Received workload message | Queue: {} | Trainer: {} | Action: {}",
                    "trainer.workload.queue",
                    request != null ? request.getTrainerUsername() : "null",
                    request != null ? request.getActionType() : "null");

            if (request == null) {
                logger.error("[TRANSACTION-ERROR] Received null workload request | Response: 400 BAD_REQUEST");
                throw new ValidationException("Received null workload request");
            }

            logger.debug("[TRANSACTION-DETAILS] Request details | Username: {} | FirstName: {} | LastName: {} | Date: {} | Duration: {} | Active: {}",
                    request.getTrainerUsername(),
                    request.getTrainerFirstName(),
                    request.getTrainerLastName(),
                    request.getTrainingDate(),
                    request.getTrainingDuration(),
                    request.getIsActive());

            Set<ConstraintViolation<TrainerWorkloadRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("Validation failed: ");
                for (ConstraintViolation<TrainerWorkloadRequest> violation : violations) {
                    errorMessage.append(violation.getPropertyPath())
                            .append(" - ")
                            .append(violation.getMessage())
                            .append("; ");
                }
                logger.error("[TRANSACTION-ERROR] Validation failed | Violations: {} | Response: 400 BAD_REQUEST", errorMessage);
                throw new ValidationException(errorMessage.toString());
            }

            if (request.getActionType() == ActionType.ADD) {
                workloadService.addTraining(request);
            } else if (request.getActionType() == ActionType.DELETE) {
                workloadService.deleteTraining(request);
            } else {
                logger.error("[TRANSACTION-ERROR] Invalid action type: {} | Response: 400 BAD_REQUEST", request.getActionType());
                throw new ValidationException("Invalid action type: " + request.getActionType());
            }

            logger.info("[TRANSACTION-SUCCESS] Successfully processed workload update | Trainer: {} | Action: {} | Response: 200 OK",
                    request.getTrainerUsername(),
                    request.getActionType());

        } catch (ValidationException e) {
            logger.error("[TRANSACTION-ERROR] Validation error | Message: {} | Response: 400 BAD_REQUEST",
                    e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("[TRANSACTION-ERROR] Failed to process workload update | Trainer: {} | Response: 500 INTERNAL_SERVER_ERROR | Error: {}",
                    request != null ? request.getTrainerUsername() : "null",
                    e.getMessage(),
                    e);
            throw e;
        } finally {
            logger.info("[TRANSACTION-END] Transaction completed | TransactionId: {}", transactionId);
            TransactionContext.clear();
        }
    }
}
