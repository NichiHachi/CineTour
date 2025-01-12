#!/bin/bash

# Function to start the application
start_application() {
    echo "Starting application with profile: $SPRING_PROFILES_ACTIVE"
    exec java -jar app.jar
}

# Check the active profile
if [ "$SPRING_PROFILES_ACTIVE" = "import" ]; then
    echo "Starting application in import mode..."
    java -jar app.jar

    # Check the exit status of the import process
    if [ $? -eq 0 ]; then
        echo "Import completed. Restarting in default mode..."
        export SPRING_PROFILES_ACTIVE=default
        start_application
    else
        echo "Error during import. Exiting..."
        exit 1
    fi
else
    start_application
fi