## Use the official OpenJDK image for Java 11 as the base image
#FROM openjdk:11-jdk-slim
#
## Set the working directory inside the container
#WORKDIR /app
#
## Copy the Spring Boot JAR file and application-prod.yml into the container
#COPY target/mockpit-1.0.0-RELEASE.jar /app/mockpit-1.0.0-RELEASE.jar
#COPY src/main/resources/application.yml /app/application.yml
#
#EXPOSE 8080
#
#ENV SPRING_CONFIG_LOCATION=/app/application.yml
#ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mockpitdb
#ENV SPRING_DATASOURCE_USERNAME=mockpituser
#ENV SPRING_DATASOURCE_PASSWORD=mockpitpassword
#
#CMD ["java", "-jar", "/app/mockpit-1.0.0-RELEASE.jar"]

# Use an official Maven image as the base image
FROM maven:3.8.4-openjdk-11 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .

# Build the project dependencies, but don't package the app yet
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the Spring Boot application
RUN mvn package -DskipTests

# Create a new Docker image with the application
FROM openjdk:11-jre-slim

# Set the working directory
WORKDIR /app

# Copy the compiled JAR file from the build stage
COPY --from=build /app/target/mockpit-*.jar mockpit.jar

# Expose the port that the Spring Boot app listens on
EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-jar", "mockpit.jar"]