package com.gym.crm.workload.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.gym.crm.workload.repository")
@EnableTransactionManagement
public class JpaConfig {}