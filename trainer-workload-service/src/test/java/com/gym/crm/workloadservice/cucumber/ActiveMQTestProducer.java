package com.gym.crm.workloadservice.cucumber;

import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class ActiveMQTestProducer {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${activemq.queues.trainer-workload}")
    private String queueName;

    public void sendWorkloadUpdate(TrainerWorkloadRequest request) {
        jmsTemplate.convertAndSend(queueName, request);
    }

    public void sendWorkloadUpdateAndWait(TrainerWorkloadRequest request, long delayMs) {
        sendWorkloadUpdate(request);
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
