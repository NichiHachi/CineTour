FROM gradle:jdk23

WORKDIR /cinetour_backend

# Copy the source code
COPY src src

# Copy the start script and make it executable
COPY start.sh /start.sh
RUN chmod +x /start.sh

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Run Gradle commands to build the project
RUN chmod +x ./gradlew && ./gradlew build -x test

EXPOSE 8080

# Set the entry point to run the entrypoint script
ENTRYPOINT ["/start.sh"]