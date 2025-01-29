FROM eclipse-temurin:23-jdk

WORKDIR /my-project

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy the source code
COPY src src

# Run Gradle commands to build the project
RUN chmod +x ./gradlew && ./gradlew clean build -x test

# Copy the generated JAR file
COPY build/libs/*.jar app.jar

# Copy the entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Set the entry point to run the entrypoint script
ENTRYPOINT ["/entrypoint.sh"]
