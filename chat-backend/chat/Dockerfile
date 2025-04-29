FROM maven:3.9.2-eclipse-temurin-17 AS builder

WORKDIR /app
COPY chat-backend/chat/ .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app
COPY --from=builder /app/target/chat-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
