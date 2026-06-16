FROM gradle:9.5.1-jdk25 AS builder
WORKDIR /app

COPY . .

RUN ./gradlew bootJar -x test

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/notification_service-*.jar app.jar

EXPOSE 6300

ENTRYPOINT ["java", "-jar", "app.jar"]