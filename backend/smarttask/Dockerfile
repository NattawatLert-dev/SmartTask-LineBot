# Stage 1: Build Spring Boot application
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src/ src/
COPY certs/ certs/

RUN ./mvnw clean package -DskipTests


# Stage 2: Run application
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/certs/tidb-truststore.p12 /app/certs/tidb-truststore.p12

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]