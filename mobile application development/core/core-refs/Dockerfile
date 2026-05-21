# Этап 1: Сборка приложения
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/refs-0.0.1.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]