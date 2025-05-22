#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting UaiFood Payment Service build process...${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running. Please start Docker and try again.${NC}"
    exit 1
fi

# Check if we're in the correct directory
if [ ! -f "build.gradle.kts" ]; then
    echo -e "${RED}Error: Please run this script from the uaifood-payment-service directory${NC}"
    exit 1
fi

# Create Gradle wrapper if it doesn't exist
if [ ! -f "gradlew" ]; then
    echo -e "${YELLOW}Creating Gradle wrapper...${NC}"
    mkdir -p gradle/wrapper
    
    # Download Gradle wrapper files
    curl -o gradle/wrapper/gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar
    curl -o gradle/wrapper/gradle-wrapper.properties https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.properties
    curl -o gradlew https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradlew
    
    # Set proper permissions
    chmod +x gradlew
    chmod 644 gradle/wrapper/gradle-wrapper.jar
    chmod 644 gradle/wrapper/gradle-wrapper.properties
fi

# Ensure gradlew is executable
chmod +x gradlew

# Start required services using Docker Compose
echo -e "${YELLOW}Starting required services (PostgreSQL and RabbitMQ)...${NC}"
docker-compose up -d

# Wait for services to be healthy
echo -e "${YELLOW}Waiting for services to be ready...${NC}"
sleep 10

# Build the application
echo -e "${YELLOW}Building the application...${NC}"
./gradlew clean build -x test

# Check if build was successful
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Build successful!${NC}"
    echo -e "${YELLOW}Starting the application...${NC}"
    
    # Run the application
    ./gradlew bootRun
else
    echo -e "${RED}Build failed! Please check the errors above.${NC}"
    exit 1
fi

# Function to handle cleanup on script exit
cleanup() {
    echo -e "${YELLOW}Stopping services...${NC}"
    docker-compose down
    echo -e "${GREEN}Cleanup complete!${NC}"
}

# Register the cleanup function to be called on script exit
trap cleanup EXIT