package com.gym.crm.workloadservice.repository.impl;

import com.gym.crm.workload.entity.TrainerWorkload;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import com.gym.crm.workload.repository.impl.TrainerWorkloadRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({TrainerWorkloadRepositoryImpl.class})
@ActiveProfiles("test")
public class TrainerWorkloadRepositoryImplTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("com.gym.crm.workload.entity")
    static class TestConfig { }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TrainerWorkloadRepository repository;

    @Test
    void testSaveNewWorkload() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername("test.trainer")
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .build();

        TrainerWorkload saved = repository.save(workload);

        assertNotNull(saved.getId());
        assertEquals("test.trainer", saved.getTrainerUsername());
    }

    @Test
    void testFindByTrainerUsername() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername("test.trainer")
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .build();

        entityManager.persistAndFlush(workload);

        Optional<TrainerWorkload> found = repository.findByTrainerUsername("test.trainer");

        assertTrue(found.isPresent());
        assertEquals("test.trainer", found.get().getTrainerUsername());
    }

    @Test
    void testExistsByTrainerUsername() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername("existing.trainer")
                .trainerFirstName("Existing")
                .trainerLastName("Trainer")
                .isActive(true)
                .build();

        entityManager.persistAndFlush(workload);

        assertTrue(repository.existsByTrainerUsername("existing.trainer"));
        assertFalse(repository.existsByTrainerUsername("nonexistent"));
    }

    @Test
    void testDelete() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername("delete.trainer")
                .trainerFirstName("Delete")
                .trainerLastName("Trainer")
                .isActive(true)
                .build();

        workload = entityManager.persistAndFlush(workload);

        repository.delete(workload);
        entityManager.flush();

        assertFalse(repository.existsByTrainerUsername("delete.trainer"));
    }
}