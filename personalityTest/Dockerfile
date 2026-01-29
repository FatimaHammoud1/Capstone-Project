# Use a base image with Java 21 (since your POM specifies java.version 21)
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and project definition first (for better caching)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Give execution rights to the Maven wrapper
RUN chmod +x mvnw

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline

# Copy the source code
COPY src ./src

# Build the application
# -DskipTests speeds up the build (optional, but good for fast deployment)
RUN ./mvnw clean package -DskipTests

# Expose the port the app runs on
EXPOSE 8080

# The command to run the application
# We look for the jar file in the target directory
CMD ["java", "-jar", "target/personalityTest-0.0.1-SNAPSHOT.jar"]
