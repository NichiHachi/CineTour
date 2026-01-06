#!/bin/bash

# Neo4j Setup Script for CineTour
# This script helps set up and migrate to Neo4j database

set -e

echo "=================================="
echo "CineTour Neo4j Setup Script"
echo "=================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

print_info "Docker and Docker Compose are installed."

# Stop and remove existing MySQL containers if any
print_info "Cleaning up old MySQL containers..."
docker-compose down -v 2>/dev/null || true

# Remove old volumes if they exist
print_warning "Removing old MySQL volumes..."
docker volume rm backend_mysql-data 2>/dev/null || true

print_info "Skipping Gradle build - Docker will build the application..."
print_info "Note: If you need to build locally, ensure Java 23 is installed or update build.gradle"

# Start Neo4j and other services
print_info "Starting Neo4j and application services..."
docker-compose up -d

# Wait for Neo4j to be ready
print_info "Waiting for Neo4j to be ready..."
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if docker exec cinetour-neo4j cypher-shell -u neo4j -p neo4jpassword "RETURN 1" &> /dev/null; then
        print_info "Neo4j is ready!"
        break
    fi
    attempt=$((attempt + 1))
    echo -n "."
    sleep 2
done

echo ""

if [ $attempt -eq $max_attempts ]; then
    print_error "Neo4j failed to start within the expected time."
    print_info "Check logs with: docker logs cinetour-neo4j"
    exit 1
fi

# Display service information
echo ""
echo "=================================="
echo "Setup Complete!"
echo "=================================="
echo ""
print_info "Services are running:"
echo "  - Neo4j Browser: http://localhost:7474"
echo "    Username: neo4j"
echo "    Password: neo4jpassword"
echo ""
echo "  - Backend API: http://localhost:9001"
echo ""
echo "  - Selenium Firefox: http://localhost:4444"
echo ""

print_info "Useful commands:"
echo "  - View logs: docker-compose logs -f"
echo "  - Stop services: docker-compose down"
echo "  - Restart services: docker-compose restart"
echo "  - Neo4j logs: docker logs cinetour-neo4j"
echo "  - Backend logs: docker logs cinetour-backend"
echo ""

print_info "Neo4j Cypher queries you can try in the browser:"
echo "  - View all nodes: MATCH (n) RETURN n LIMIT 25"
echo "  - View all movies: MATCH (m:Movie) RETURN m"
echo "  - View users and search history: MATCH (u:User)-[:HAS_SEARCH_HISTORY]->(msh:MovieSearchHistory) RETURN u, msh"
echo ""

print_warning "Note: If you need to import movies, set SPRING_PROFILES_ACTIVE=import in docker-compose.yml"
echo ""

print_info "Check the NEO4J_MIGRATION_GUIDE.md for more information"
