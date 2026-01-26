package com.gym.crm.workloadservice.repository;

import com.gym.crm.workload.entity.TrainerWorkload;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainerWorkloadRepositoryTest {

    @Test
    void repositoryInterface_ShouldExtendMongoRepository() {
        assertTrue(MongoRepository.class.isAssignableFrom(TrainerWorkloadRepository.class));
    }

    @Test
    void repositoryInterface_ShouldHaveFindByTrainerUsernameMethod() {
        Method[] methods = TrainerWorkloadRepository.class.getDeclaredMethods();
        boolean hasMethod = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("findByTrainerUsername") &&
                        m.getReturnType().equals(Optional.class));

        assertTrue(hasMethod);
    }

    @Test
    void repositoryInterface_ShouldHaveExistsByTrainerUsernameMethod() {
        Method[] methods = TrainerWorkloadRepository.class.getDeclaredMethods();
        boolean hasMethod = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("existsByTrainerUsername") &&
                        m.getReturnType().equals(boolean.class));

        assertTrue(hasMethod);
    }

    @Test
    void repositoryInterface_ShouldHaveDeleteByTrainerUsernameMethod() {
        Method[] methods = TrainerWorkloadRepository.class.getDeclaredMethods();
        boolean hasMethod = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("deleteByTrainerUsername") &&
                        m.getReturnType().equals(void.class));

        assertTrue(hasMethod);
    }

    @Test
    void repositoryInterface_ShouldHaveFindByTrainerFirstNameMethod() {
        Method[] methods = TrainerWorkloadRepository.class.getDeclaredMethods();
        boolean hasMethod = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("findByTrainerFirstName") &&
                        m.getReturnType().equals(List.class));

        assertTrue(hasMethod);
    }

    @Test
    void repositoryInterface_ShouldHaveFindByTrainerLastNameMethod() {
        Method[] methods = TrainerWorkloadRepository.class.getDeclaredMethods();
        boolean hasMethod = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("findByTrainerLastName") &&
                        m.getReturnType().equals(List.class));

        assertTrue(hasMethod);
    }

    @Test
    void repositoryInterface_ShouldHaveFindByTrainerFirstNameAndTrainerLastNameMethod() {
        Method[] methods = TrainerWorkloadRepository.class.getDeclaredMethods();
        boolean hasMethod = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("findByTrainerFirstNameAndTrainerLastName") &&
                        m.getReturnType().equals(List.class));

        assertTrue(hasMethod);
    }

    @Test
    void repositoryInterface_ShouldWorkWithStringIdType() {
        Method[] methods = MongoRepository.class.getMethods();
        boolean hasSaveMethod = Arrays.stream(methods)
                .anyMatch(m -> m.getName().equals("save"));

        assertTrue(hasSaveMethod);
    }
}
