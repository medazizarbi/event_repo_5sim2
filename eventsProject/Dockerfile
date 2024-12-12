# Use an OpenJDK image as the base
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the target directory to the container
COPY target/eventsProject-1.0.0.jar app.jar

# Expose the port that your app runs on
EXPOSE 8089

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
