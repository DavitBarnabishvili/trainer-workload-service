package com.gym.crm.workloadservice.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestActiveMQConfig {

    @Bean
    @Primary
    public ActiveMQConnectionFactory jmsConnectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");

        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(0);
        factory.setRedeliveryPolicy(redeliveryPolicy);
        factory.setTrustAllPackages(true);

        return factory;
    }
}
