package com.gym.crm.workload.controller;

import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.dto.TrainerWorkloadResponse;
import com.gym.crm.workload.service.TrainerWorkloadService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/trainer/workload")
public class TrainerWorkloadController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadController.class);

    private final TrainerWorkloadService workloadService;

    public TrainerWorkloadController(TrainerWorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @PostMapping
    public ResponseEntity<Void> updateTrainerWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Transaction [{}] - Received workload update request for trainer: {} with action: {}",
                transactionId, request.getTrainerUsername(), request.getActionType());

        try {
            workloadService.updateWorkload(request);
            logger.info("Transaction [{}] - Successfully processed workload update for trainer: {}",
                    transactionId, request.getTrainerUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Transaction [{}] - Failed to process workload update", transactionId, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadResponse> getTrainerWorkload(@PathVariable String username) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        logger.info("Transaction [{}] - Fetching workload for trainer: {}", transactionId, username);

        try {
            TrainerWorkloadResponse response = workloadService.getTrainerWorkload(username);
            logger.info("Transaction [{}] - Successfully fetched workload for trainer: {}",
                    transactionId, username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Transaction [{}] - Failed to fetch workload", transactionId, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}