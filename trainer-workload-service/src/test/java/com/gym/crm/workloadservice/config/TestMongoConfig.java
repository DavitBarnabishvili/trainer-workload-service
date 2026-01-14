package com.gym.crm.workloadservice.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.commands.ServerAddress;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import jakarta.annotation.PreDestroy;
import com.gym.crm.workloadservice.cucumber.ActiveMQTestProducer;
import com.gym.crm.workloadservice.cucumber.TestContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@TestConfiguration
public class TestMongoConfig {

    private TransitionWalker.ReachedState<RunningMongodProcess> runningMongod;

    @Bean
    @Primary
    public MongoClient mongoClient() {
        runningMongod = Mongod.builder()
                .mongodArguments(Start.to(MongodArguments.class)
                        .initializedWith(MongodArguments.defaults()))
                .processOutput(Start.to(ProcessOutput.class)
                        .initializedWith(ProcessOutput.silent()))
                .build()
                .start(Version.Main.V6_0);

        ServerAddress serverAddress = runningMongod.current().getServerAddress();
        String connectionString = "mongodb://" + serverAddress.getHost() + ":" + serverAddress.getPort();

        return MongoClients.create(connectionString);
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient, "test_trainer_workload_db"));
    }

    @Bean
    public TestContext testContext() {
        return new TestContext();
    }

    @Bean
    public ActiveMQTestProducer activeMQTestProducer() {
        return new ActiveMQTestProducer();
    }

    @PreDestroy
    public void shutdown() {
        if (runningMongod != null) {
            runningMongod.close();
        }
    }
}
