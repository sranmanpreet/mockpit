# Use the official OpenJDK image for Java 11 as the base image
FROM openjdk:11-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot JAR file and application-prod.yml into the container
COPY target/mockpit-0.0.1-SNAPSHOT.jar /app/mockpit-0.0.1-SNAPSHOT.jar
COPY src/main/resources/application.yml /app/application.yml

EXPOSE 8080

ENV SPRING_CONFIG_LOCATION=/app/application.yml
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mockpitdb
ENV SPRING_DATASOURCE_USERNAME=mockpituser
ENV SPRING_DATASOURCE_PASSWORD=mockpitpassword

CMD ["java", "-jar", "/app/mockpit-0.0.1-SNAPSHOT.jar"]
