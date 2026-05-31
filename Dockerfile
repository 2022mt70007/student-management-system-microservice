# Multi-stage build for any SMS microservice module
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
ARG MODULE
WORKDIR /app

COPY pom.xml .
COPY common-lib/pom.xml common-lib/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY admin-service/pom.xml admin-service/pom.xml
COPY student-service/pom.xml student-service/pom.xml
COPY teacher-service/pom.xml teacher-service/pom.xml
COPY course-service/pom.xml course-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN test -n "$MODULE" || (echo "ERROR: MODULE build arg is required (e.g. auth-service)" && exit 1)

COPY common-lib common-lib
COPY eureka-server eureka-server
COPY api-gateway api-gateway
COPY auth-service auth-service
COPY admin-service admin-service
COPY student-service student-service
COPY teacher-service teacher-service
COPY course-service course-service
COPY notification-service notification-service

RUN mvn clean package -B -pl "${MODULE}" -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
ARG MODULE
WORKDIR /app
COPY --from=build /app/${MODULE}/target/${MODULE}-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
