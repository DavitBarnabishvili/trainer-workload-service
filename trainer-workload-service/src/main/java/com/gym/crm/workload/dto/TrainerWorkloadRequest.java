package com.gym.crm.workload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update trainer workload")
public class TrainerWorkloadRequest {

    @NotBlank(message = "Trainer username is required")
    @Size(min = 3, max = 50, message = "Trainer username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Trainer username must contain only letters, numbers, dots, underscores, and hyphens")
    @Schema(description = "Trainer's username", example = "trainer.one")
    private String trainerUsername;

    @NotBlank(message = "Trainer first name is required")
    @Size(min = 1, max = 50, message = "Trainer first name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Trainer first name must contain only letters")
    @Schema(description = "Trainer's first name", example = "John")
    private String trainerFirstName;

    @NotBlank(message = "Trainer last name is required")
    @Size(min = 1, max = 50, message = "Trainer last name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Trainer last name must contain only letters")
    @Schema(description = "Trainer's last name", example = "Doe")
    private String trainerLastName;

    @NotNull(message = "Active status is required")
    @Schema(description = "Trainer's active status", example = "true")
    private Boolean isActive;

    @NotNull(message = "Training date is required")
    @PastOrPresent(message = "Training date cannot be in the future")
    @Schema(description = "Date of the training session", example = "2025-11-20")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    @Max(value = 480, message = "Training duration cannot exceed 480 minutes (8 hours)")
    @Schema(description = "Duration of training in minutes", example = "60", minimum = "1", maximum = "480")
    private Integer trainingDuration;

    @NotNull(message = "Action type is required")
    @Schema(description = "Action to perform on workload", example = "ADD")
    private ActionType actionType;

    @Schema(description = "Transaction ID for request tracing", example = "123e4567-e89b-12d3-a456-426614174000")
    private String transactionId;
}