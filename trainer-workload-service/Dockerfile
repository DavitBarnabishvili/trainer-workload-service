# Dockerfile for Trainer Workload Microservice

FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

# Build with local profile (includes embedded MongoDB dependencies)
RUN mvn clean package -DskipTests -Plocal


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring -m -d /home/spring spring

COPY --from=builder /app/target/trainer-workload-service-1.0.0.jar app.jar

RUN chown -R spring:spring /app /home/spring

USER spring

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=local"]
