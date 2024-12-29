FROM eclipse-temurin:23-jdk

WORKDIR /my-project

# Copy the project files
COPY . .

# Run Gradle commands to build the project
RUN chmod +x ./gradlew && ./gradlew clean build -x test

# Copy the generated JAR file
COPY build/libs/*.jar app.jar

# Expose the application port (uncomment if needed)
# EXPOSE 8080

# Set the entry point to run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]