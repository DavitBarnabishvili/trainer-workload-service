package com.gym.crm.workloadservice.cucumber;

import com.gym.crm.workload.TrainerWorkloadApplication;
import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.gym.crm.workloadservice.config.TestActiveMQConfig;
import com.gym.crm.workloadservice.config.TestMongoConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(
    classes = TrainerWorkloadApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import({TestMongoConfig.class, TestActiveMQConfig.class})
@ActiveProfiles("test")
public class CucumberHooks {

    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private TestContext testContext;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${activemq.queues.trainer-workload}")
    private String queueName;

    @Before
    public void beforeScenario() {
        testContext.reset();
        purgeQueue();
    }

    @After
    public void afterScenario() {
        repository.deleteAll();
        purgeQueue();
        testContext.reset();
    }

    private void purgeQueue() {
        try {
            jmsTemplate.setReceiveTimeout(100);
            while (jmsTemplate.receive(queueName) != null) {
            }
        } catch (Exception e) {
        }
    }
}
