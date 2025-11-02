package com.gym.crm.workload.repository;

import com.gym.crm.workload.entity.TrainerWorkload;

import java.util.Optional;
import java.util.List;

public interface TrainerWorkloadRepository {
    TrainerWorkload save(TrainerWorkload workload);
    Optional<TrainerWorkload> findById(Long id);
    Optional<TrainerWorkload> findByTrainerUsername(String trainerUsername);
    boolean existsByTrainerUsername(String trainerUsername);
    List<TrainerWorkload> findAll();
    void delete(TrainerWorkload workload);
}