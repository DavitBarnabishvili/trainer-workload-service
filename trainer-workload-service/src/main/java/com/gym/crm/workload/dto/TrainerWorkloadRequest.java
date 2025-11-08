package com.gym.crm.workload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update trainer workload")
public class TrainerWorkloadRequest {

    @NotBlank(message = "Trainer username is required")
    @Schema(description = "Trainer's username", example = "trainer.one")
    private String trainerUsername;

    @NotBlank(message = "Trainer first name is required")
    @Schema(description = "Trainer's first name", example = "John")
    private String trainerFirstName;

    @NotBlank(message = "Trainer last name is required")
    @Schema(description = "Trainer's last name", example = "Doe")
    private String trainerLastName;

    @NotNull(message = "Active status is required")
    @Schema(description = "Trainer's active status", example = "true")
    private Boolean isActive;

    @NotNull(message = "Training date is required")
    @Schema(description = "Date of the training session", example = "2025-11-20")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    @Schema(description = "Duration of training in minutes", example = "60", minimum = "1", maximum = "480")
    private Integer trainingDuration;

    @NotNull(message = "Action type is required")
    @Schema(description = "Action to perform on workload", example = "ADD")
    private ActionType actionType;
}