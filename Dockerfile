# Root Dockerfile for Backend (Fixes path issues)
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy Maven Wrapper and Project Definition
# Note: Paths are relative to repo root now
COPY personalityTest/.mvn/ .mvn
COPY personalityTest/mvnw personalityTest/pom.xml ./

# Fix line endings
RUN chmod +x mvnw && \
    sed -i 's/\r$//' mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy Source Code
COPY personalityTest/src ./src

# Build
RUN ./mvnw clean package -DskipTests

# Run
EXPOSE 8080
CMD ["java", "-jar", "target/personalityTest-0.0.1-SNAPSHOT.jar"]
