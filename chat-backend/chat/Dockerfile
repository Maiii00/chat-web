FROM openjdk:17-jdk-slim

WORKDIR /chat-backend

COPY ./target/chat-0.0.1-SNAPSHOT.jar app.jar

# 讓 Spring Boot 在 Docker 內使用 host.docker.internal 訪問 Redis
ENV SPRING_DATA_REDIS_HOST=host.docker.internal

ENTRYPOINT ["java", "-jar", "app.jar"]