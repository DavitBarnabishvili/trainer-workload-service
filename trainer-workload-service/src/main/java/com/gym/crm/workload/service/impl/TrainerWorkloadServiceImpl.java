package com.gym.crm.workload.service.impl;

import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.dto.TrainerWorkloadResponse;
import com.gym.crm.workload.entity.TrainerWorkload;
import com.gym.crm.workload.exception.WorkloadNotFoundException;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import com.gym.crm.workload.service.TrainerWorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@Transactional
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadServiceImpl.class);

    private final TrainerWorkloadRepository repository;

    public TrainerWorkloadServiceImpl(TrainerWorkloadRepository repository) {
        this.repository = repository;
    }

    @Override
    public void updateWorkload(TrainerWorkloadRequest request) {
        logger.info("Updating workload for trainer: {} with action: {}",
                request.getTrainerUsername(), request.getActionType());

        TrainerWorkload workload = repository.findByTrainerUsername(request.getTrainerUsername())
                .orElseGet(() -> createNewWorkload(request));

        // Update trainer info in case it changed
        workload.setTrainerFirstName(request.getTrainerFirstName());
        workload.setTrainerLastName(request.getTrainerLastName());
        workload.setIsActive(request.getIsActive());

        LocalDate trainingDate = request.getTrainingDate();
        int year = trainingDate.getYear();
        int month = trainingDate.getMonthValue();
        int duration = request.getTrainingDuration();

        if (request.getActionType() == ActionType.ADD) {
            workload.addOrUpdateTraining(year, month, duration);
            logger.info("Added {} minutes to {}/{} for trainer: {}",
                    duration, month, year, request.getTrainerUsername());
        } else if (request.getActionType() == ActionType.DELETE) {
            workload.removeTraining(year, month, duration);
            logger.info("Removed {} minutes from {}/{} for trainer: {}",
                    duration, month, year, request.getTrainerUsername());
        }

        repository.save(workload);
        logger.info("Successfully updated workload for trainer: {}", request.getTrainerUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerWorkloadResponse getTrainerWorkload(String username) {
        logger.info("Fetching workload for trainer: {}", username);

        TrainerWorkload workload = repository.findByTrainerUsername(username)
                .orElseThrow(() -> new WorkloadNotFoundException("Trainer workload not found for username: " + username));

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

        logger.info("Successfully fetched workload for trainer: {}", username);
        return response;
    }

    private TrainerWorkload createNewWorkload(TrainerWorkloadRequest request) {
        logger.info("Creating new workload entry for trainer: {}", request.getTrainerUsername());

        return TrainerWorkload.builder()
                .trainerUsername(request.getTrainerUsername())
                .trainerFirstName(request.getTrainerFirstName())
                .trainerLastName(request.getTrainerLastName())
                .isActive(request.getIsActive())
                .build();
    }
}