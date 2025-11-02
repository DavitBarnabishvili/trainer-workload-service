package com.gym.crm.workload.repository.impl;

import com.gym.crm.workload.entity.TrainerWorkload;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class TrainerWorkloadRepositoryImpl implements TrainerWorkloadRepository {

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public TrainerWorkload save(TrainerWorkload workload) {
        if (workload == null) {
            throw new IllegalArgumentException("Workload cannot be null");
        }

        if (workload.getId() == null) {
            logger.debug("Creating new trainer workload for username: {}", workload.getTrainerUsername());
            entityManager.persist(workload);
            logger.info("Successfully created trainer workload for username: {}", workload.getTrainerUsername());
            return workload;
        } else {
            logger.debug("Updating trainer workload with id: {}", workload.getId());
            TrainerWorkload mergedWorkload = entityManager.merge(workload);
            logger.info("Successfully updated trainer workload with id: {}", workload.getId());
            return mergedWorkload;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerWorkload> findById(Long id) {
        if (id == null) {
            logger.debug("FindById called with null id");
            return Optional.empty();
        }

        logger.debug("Finding trainer workload by id: {}", id);
        TrainerWorkload workload = entityManager.find(TrainerWorkload.class, id);

        if (workload != null) {
            logger.debug("Found trainer workload with id: {}", id);
        } else {
            logger.debug("No trainer workload found with id: {}", id);
        }

        return Optional.ofNullable(workload);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerWorkload> findByTrainerUsername(String trainerUsername) {
        if (trainerUsername == null || trainerUsername.trim().isEmpty()) {
            logger.debug("FindByTrainerUsername called with null or empty username");
            return Optional.empty();
        }

        logger.debug("Finding trainer workload by username: {}", trainerUsername);

        try {
            TypedQuery<TrainerWorkload> query = entityManager.createQuery(
                    "SELECT tw FROM TrainerWorkload tw " +
                            "LEFT JOIN FETCH tw.years y " +
                            "LEFT JOIN FETCH y.months " +
                            "WHERE tw.trainerUsername = :username",
                    TrainerWorkload.class
            );
            query.setParameter("username", trainerUsername);

            TrainerWorkload workload = query.getSingleResult();
            logger.debug("Found trainer workload for username: {}", trainerUsername);
            return Optional.of(workload);

        } catch (NoResultException e) {
            logger.debug("No trainer workload found for username: {}", trainerUsername);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTrainerUsername(String trainerUsername) {
        if (trainerUsername == null || trainerUsername.trim().isEmpty()) {
            return false;
        }

        logger.debug("Checking if trainer workload exists for username: {}", trainerUsername);

        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(tw) FROM TrainerWorkload tw WHERE tw.trainerUsername = :username",
                Long.class
        );
        query.setParameter("username", trainerUsername);

        Long count = query.getSingleResult();
        boolean exists = count > 0;

        logger.debug("Trainer workload exists for username {}: {}", trainerUsername, exists);
        return exists;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerWorkload> findAll() {
        logger.debug("Finding all trainer workloads");

        TypedQuery<TrainerWorkload> query = entityManager.createQuery(
                "SELECT DISTINCT tw FROM TrainerWorkload tw " +
                        "LEFT JOIN FETCH tw.years y " +
                        "LEFT JOIN FETCH y.months " +
                        "ORDER BY tw.trainerUsername",
                TrainerWorkload.class
        );

        List<TrainerWorkload> workloads = query.getResultList();
        logger.debug("Found {} trainer workloads", workloads.size());

        return workloads;
    }

    @Override
    public void delete(TrainerWorkload workload) {
        if (workload == null) {
            throw new IllegalArgumentException("Workload cannot be null");
        }

        if (workload.getId() == null) {
            throw new IllegalArgumentException("Workload ID cannot be null");
        }

        logger.debug("Deleting trainer workload with id: {}", workload.getId());

        TrainerWorkload managedWorkload = entityManager.find(TrainerWorkload.class, workload.getId());
        if (managedWorkload != null) {
            entityManager.remove(managedWorkload);
            logger.info("Successfully deleted trainer workload with id: {}", workload.getId());
        } else {
            logger.warn("Trainer workload not found for deletion with id: {}", workload.getId());
        }
    }
}