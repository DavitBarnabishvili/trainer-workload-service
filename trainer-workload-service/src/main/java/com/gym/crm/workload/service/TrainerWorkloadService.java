package com.gym.crm.workload.service;

import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.dto.TrainerWorkloadResponse;

public interface TrainerWorkloadService {
    void updateWorkload(TrainerWorkloadRequest request);
    TrainerWorkloadResponse getTrainerWorkload(String username);
}