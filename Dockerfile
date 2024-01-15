# Base image
FROM openjdk:8-jre-slim

# Copy the JAR file into the container
COPY target/my-cool-service-1.0.0.jar /my-cool-service-1.0.0.jar

# Expose the port that the application listens on
EXPOSE 8000

# Set the entrypoint command to run the application when the container starts
CMD ["java", "-jar", "/my-cool-service-1.0.0.jar"]