#!/bin/bash
echo "====================================="
echo "  CineTour Import Progress Monitor"
echo "====================================="
echo ""

while true; do
    clear
    echo "====================================="
    echo "  CineTour Import Progress"
    echo "====================================="
    echo ""
    
    # Check if container is running
    if ! docker ps | grep -q cinetour-backend; then
        echo "❌ Backend container is not running!"
        echo ""
        echo "Import may have completed. Check with:"
        echo "  docker logs cinetour-backend | tail -50"
        break
    fi
    
    # Show latest import messages
    echo "📋 Latest Activity:"
    docker logs cinetour-backend 2>&1 | grep -E "Import mode|Downloading|imported|Movies|Locations" | tail -5
    echo ""
    
    # Count movies in Neo4j
    echo "📊 Current Data Count:"
    MOVIE_COUNT=$(docker exec cinetour-neo4j cypher-shell -u neo4j -p neo4jpassword "MATCH (m:Movie) RETURN count(m)" 2>/dev/null | tail -1)
    LOCATION_COUNT=$(docker exec cinetour-neo4j cypher-shell -u neo4j -p neo4jpassword "MATCH (l:Location) RETURN count(l)" 2>/dev/null | tail -1)
    
    echo "  Movies: $MOVIE_COUNT"
    echo "  Locations: $LOCATION_COUNT"
    echo ""
    
    echo "Press Ctrl+C to stop monitoring"
    echo ""
    
    sleep 10
done
