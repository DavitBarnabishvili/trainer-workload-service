package com.gym.crm.workload.repository;

import com.gym.crm.workload.entity.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {
    Optional<TrainerWorkload> findByTrainerUsername(String trainerUsername);
    boolean existsByTrainerUsername(String trainerUsername);
    void deleteByTrainerUsername(String trainerUsername);
    List<TrainerWorkload> findByTrainerFirstName(String trainerFirstName);
    List<TrainerWorkload> findByTrainerLastName(String trainerLastName);
    List<TrainerWorkload> findByTrainerFirstNameAndTrainerLastName(String trainerFirstName, String trainerLastName);
}