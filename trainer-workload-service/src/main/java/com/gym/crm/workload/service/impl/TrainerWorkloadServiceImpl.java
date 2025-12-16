package com.gym.crm.workload.service.impl;

import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.dto.TrainerWorkloadResponse;
import com.gym.crm.workload.entity.TrainerWorkload;
import com.gym.crm.workload.exception.ValidationException;
import com.gym.crm.workload.exception.WorkloadNotFoundException;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import com.gym.crm.workload.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadServiceImpl.class);

    private final TrainerWorkloadRepository repository;

    public TrainerWorkloadServiceImpl(TrainerWorkloadRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addTraining(TrainerWorkloadRequest request) {
        logger.debug("[OPERATION] Validating request");
        validateRequest(request);
        logger.debug("[OPERATION] Request validation passed");

        logger.info("[OPERATION-START] addTraining | Trainer: {} | Duration: {} minutes",
                request.getTrainerUsername(), request.getTrainingDuration());

        logger.debug("[OPERATION] Searching for existing workload | Trainer: {}", request.getTrainerUsername());
        TrainerWorkload workload = repository.findByTrainerUsername(request.getTrainerUsername())
                .orElseGet(() -> {
                    logger.info("[OPERATION] Trainer workload not found, creating new entry | Trainer: {}", request.getTrainerUsername());
                    return createNewWorkload(request);
                });

        if (workload.getId() != null) {
            logger.debug("[OPERATION] Found existing workload | WorkloadId: {} | Trainer: {}",
                    workload.getId(), request.getTrainerUsername());
        }

        logger.debug("[OPERATION] Updating trainer information | Trainer: {}", request.getTrainerUsername());
        workload.setTrainerFirstName(request.getTrainerFirstName());
        workload.setTrainerLastName(request.getTrainerLastName());
        workload.setIsActive(request.getIsActive());

        LocalDate trainingDate = request.getTrainingDate();
        int year = trainingDate.getYear();
        int month = trainingDate.getMonthValue();
        int duration = request.getTrainingDuration();

        logger.debug("[OPERATION] Validating training data | Year: {} | Month: {} | Duration: {}", year, month, duration);
        validateTrainingData(year, month, duration);
        logger.debug("[OPERATION] Training data validation passed");

        logger.debug("[OPERATION] Adding training to workload | Year: {} | Month: {} | Duration: {} minutes",
                year, month, duration);
        workload.addOrUpdateTraining(year, month, duration);

        logger.debug("[OPERATION] Saving workload to database | Trainer: {}", request.getTrainerUsername());
        repository.save(workload);
        logger.debug("[OPERATION] Workload saved successfully");

        logger.info("[OPERATION-SUCCESS] Successfully added {} minutes to {}/{} for trainer: {}",
                duration, month, year, request.getTrainerUsername());
    }

    @Override
    public void deleteTraining(TrainerWorkloadRequest request) {
        logger.debug("[OPERATION] Validating request");
        validateRequest(request);
        logger.debug("[OPERATION] Request validation passed");

        logger.info("[OPERATION-START] deleteTraining | Trainer: {} | Duration: {} minutes",
                request.getTrainerUsername(), request.getTrainingDuration());

        logger.debug("[OPERATION] Searching for existing workload | Trainer: {}", request.getTrainerUsername());
        TrainerWorkload workload = repository.findByTrainerUsername(request.getTrainerUsername())
                .orElseThrow(() -> {
                    logger.error("[OPERATION-ERROR] Trainer workload not found | Trainer: {}", request.getTrainerUsername());
                    return new WorkloadNotFoundException("Trainer workload not found for username: " + request.getTrainerUsername());
                });

        logger.debug("[OPERATION] Found existing workload | WorkloadId: {} | Trainer: {}",
                workload.getId(), request.getTrainerUsername());

        LocalDate trainingDate = request.getTrainingDate();
        int year = trainingDate.getYear();
        int month = trainingDate.getMonthValue();
        int duration = request.getTrainingDuration();

        logger.debug("[OPERATION] Validating training data | Year: {} | Month: {} | Duration: {}", year, month, duration);
        validateTrainingData(year, month, duration);
        logger.debug("[OPERATION] Training data validation passed");

        logger.debug("[OPERATION] Removing training from workload | Year: {} | Month: {} | Duration: {} minutes",
                year, month, duration);
        workload.removeTraining(year, month, duration);

        logger.debug("[OPERATION] Saving workload to database | Trainer: {}", request.getTrainerUsername());
        repository.save(workload);
        logger.debug("[OPERATION] Workload saved successfully");

        logger.info("[OPERATION-SUCCESS] Successfully removed {} minutes from {}/{} for trainer: {}",
                duration, month, year, request.getTrainerUsername());
    }

    @Override
    public TrainerWorkloadResponse getTrainerWorkload(String username) {
        logger.info("[OPERATION-START] getTrainerWorkload | Trainer: {}", username);

        logger.debug("[OPERATION] Validating username parameter");
        if (username == null || username.trim().isEmpty()) {
            logger.error("[OPERATION-ERROR] Username is null or empty");
            throw new ValidationException("Username cannot be null or empty");
        }
        logger.debug("[OPERATION] Username validation passed");

        logger.debug("[OPERATION] Fetching workload from database | Trainer: {}", username);
        TrainerWorkload workload = repository.findByTrainerUsername(username)
                .orElseThrow(() -> {
                    logger.error("[OPERATION-ERROR] Trainer workload not found | Trainer: {}", username);
                    return new WorkloadNotFoundException("Trainer workload not found for username: " + username);
                });

        logger.debug("[OPERATION] Workload found | WorkloadId: {} | Years count: {} | Trainer: {}",
                workload.getId(), workload.getYears().size(), username);

        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername(workload.getTrainerUsername())
                .trainerFirstName(workload.getTrainerFirstName())
                .trainerLastName(workload.getTrainerLastName())
                .isActive(workload.getIsActive())
                .years(workload.getYears().stream()
                        .map(year -> TrainerWorkloadResponse.YearSummaryDto.builder()
                                .year(year.getYear())
                                .months(year.getMonths().stream()
                                        .map(month -> TrainerWorkloadResponse.MonthSummaryDto.builder()
                                                .month(month.getMonth())
                                                .monthName(month.getMonthName())
                                                .totalDuration(month.getTotalDuration())
                                                .build())
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .build();

        logger.debug("[OPERATION] Building response DTO | Trainer: {}", username);
        logger.info("[OPERATION-SUCCESS] Successfully fetched workload for trainer: {} | Years: {} | Total months: {}",
                username,
                response.getYears().size(),
                response.getYears().stream().mapToInt(y -> y.getMonths().size()).sum());
        return response;
    }

    private TrainerWorkload createNewWorkload(TrainerWorkloadRequest request) {
        logger.debug("[OPERATION] Creating new workload entry | Trainer: {} | FirstName: {} | LastName: {} | Active: {}",
                request.getTrainerUsername(),
                request.getTrainerFirstName(),
                request.getTrainerLastName(),
                request.getIsActive());

        return TrainerWorkload.builder()
                .trainerUsername(request.getTrainerUsername())
                .trainerFirstName(request.getTrainerFirstName())
                .trainerLastName(request.getTrainerLastName())
                .isActive(request.getIsActive())
                .build();
    }

    private void validateRequest(TrainerWorkloadRequest request) {
        logger.debug("[OPERATION] Validating request object");
        if (request == null) {
            logger.error("[OPERATION-ERROR] Request object is null");
            throw new ValidationException("Request cannot be null");
        }

        if (request.getTrainerUsername() == null || request.getTrainerUsername().trim().isEmpty()) {
            logger.error("[OPERATION-ERROR] Trainer username is null or empty");
            throw new ValidationException("Trainer username cannot be null or empty");
        }

        if (request.getTrainerFirstName() == null || request.getTrainerFirstName().trim().isEmpty()) {
            logger.error("[OPERATION-ERROR] Trainer first name is null or empty");
            throw new ValidationException("Trainer first name cannot be null or empty");
        }

        if (request.getTrainerLastName() == null || request.getTrainerLastName().trim().isEmpty()) {
            logger.error("[OPERATION-ERROR] Trainer last name is null or empty");
            throw new ValidationException("Trainer last name cannot be null or empty");
        }

        if (request.getIsActive() == null) {
            logger.error("[OPERATION-ERROR] Trainer active status is null");
            throw new ValidationException("Trainer active status cannot be null");
        }

        if (request.getTrainingDate() == null) {
            logger.error("[OPERATION-ERROR] Training date is null");
            throw new ValidationException("Training date cannot be null");
        }

        if (request.getTrainingDate().isAfter(LocalDate.now())) {
            logger.error("[OPERATION-ERROR] Training date is in the future | Date: {}", request.getTrainingDate());
            throw new ValidationException("Training date cannot be in the future");
        }

        if (request.getTrainingDuration() == null || request.getTrainingDuration() <= 0) {
            logger.error("[OPERATION-ERROR] Training duration is invalid | Duration: {}", request.getTrainingDuration());
            throw new ValidationException("Training duration must be a positive number");
        }

        if (request.getTrainingDuration() > 480) {
            logger.error("[OPERATION-ERROR] Training duration exceeds maximum | Duration: {} | Max: 480", request.getTrainingDuration());
            throw new ValidationException("Training duration cannot exceed 480 minutes (8 hours)");
        }

        if (request.getActionType() == null) {
            logger.error("[OPERATION-ERROR] Action type is null");
            throw new ValidationException("Action type cannot be null");
        }
        logger.debug("[OPERATION] All request validations passed");
    }

    private void validateTrainingData(int year, int month, int duration) {
        logger.debug("[OPERATION] Validating training data | Year: {} | Month: {} | Duration: {}", year, month, duration);

        if (year < 1970 || year > 2100) {
            logger.error("[OPERATION-ERROR] Year out of range | Year: {} | Valid range: 1970-2100", year);
            throw new ValidationException("Year must be between 1970 and 2100");
        }

        if (month < 1 || month > 12) {
            logger.error("[OPERATION-ERROR] Month out of range | Month: {} | Valid range: 1-12", month);
            throw new ValidationException("Month must be between 1 and 12");
        }

        if (duration <= 0) {
            logger.error("[OPERATION-ERROR] Duration is not positive | Duration: {}", duration);
            throw new ValidationException("Duration must be positive");
        }

        if (duration > 480) {
            logger.error("[OPERATION-ERROR] Duration exceeds maximum | Duration: {} | Max: 480", duration);
            throw new ValidationException("Duration cannot exceed 480 minutes");
        }
        logger.debug("[OPERATION] Training data validation passed");
    }
}
