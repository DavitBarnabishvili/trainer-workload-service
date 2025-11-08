package com.gym.crm.workload.controller;

import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.dto.TrainerWorkloadResponse;
import com.gym.crm.workload.service.TrainerWorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/trainer/workload")
@Tag(name = "Trainer Workload", description = "Endpoints for managing trainer workload and monthly summaries")
@SecurityRequirement(name = "Bearer Authentication")
public class TrainerWorkloadController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadController.class);

    private final TrainerWorkloadService workloadService;

    public TrainerWorkloadController(TrainerWorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @PostMapping
    @Operation(
            summary = "Update trainer workload",
            description = "Add or remove training hours from a trainer's monthly summary"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Workload updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<Void> updateTrainerWorkload(
            @Parameter(description = "Trainer workload update request", required = true)
            @Valid @RequestBody TrainerWorkloadRequest request) {

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
    @Operation(
            summary = "Get trainer workload",
            description = "Retrieve complete workload summary for a specific trainer including all years and months"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Workload retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TrainerWorkloadResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trainer workload not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token"
            )
    })
    public ResponseEntity<TrainerWorkloadResponse> getTrainerWorkload(
            @Parameter(description = "Trainer username", required = true, example = "trainer.one")
            @PathVariable String username) {

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