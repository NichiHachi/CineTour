package com.polytech.crud.neo4j.repository;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.polytech.crud.neo4j.entity.LocationNode;

@Repository
@Profile("!import")
public interface LocationNodeRepository extends Neo4jRepository<LocationNode, Long> {

        List<LocationNode> findByIdImdb(String idImdb);

        List<LocationNode> findByCountryCode(String countryCode);

        @Query("MATCH (l:Location) WHERE l.latitude IS NOT NULL AND l.longitude IS NOT NULL " +
                        "AND point.distance(point({latitude: l.latitude, longitude: l.longitude}), " +
                        "point({latitude: $lat, longitude: $lon})) < $distance RETURN l")
        List<LocationNode> findNearby(@Param("lat") Double latitude,
                        @Param("lon") Double longitude,
                        @Param("distance") Double distanceInMeters);

        @Query("MATCH (m:Movie)-[:FILMED_AT]->(l:Location) WHERE m.idImdb = $idImdb RETURN l")
        List<LocationNode> findByMovieIdImdb(@Param("idImdb") String idImdb);

        @Query("MATCH ()-[r:NEAR]->() DELETE r")
        void deleteAllNearRelationships();

        @Query("MATCH (l1:Location), (l2:Location) " +
                        "WHERE id(l1) = $fromId AND id(l2) = $toId " +
                        "MERGE (l1)-[r:NEAR]->(l2) SET r.distance = $distance " +
                        "MERGE (l2)-[r2:NEAR]->(l1) SET r2.distance = $distance")
        void createNearRelationship(@Param("fromId") Long fromId, @Param("toId") Long toId,
                        @Param("distance") Double distance);

        @Query("MATCH (l:Location)-[r:NEAR]->(neighbor:Location) WHERE id(l) = $locationId RETURN neighbor, r.distance as distance")
        List<LocationWithDistanceProjection> findNeighbors(@Param("locationId") Long locationId);

        @Query("MATCH path = shortestPath((from:Location)-[:NEAR*]-(to:Location)) WHERE id(from) = $fromId AND id(to) = $toId RETURN nodes(path)")
        List<LocationNode> findShortestPath(@Param("fromId") Long fromId, @Param("toId") Long toId);

        @Query("MATCH path = (start:Location)-[:NEAR*1..5]-(nearby:Location) " +
                        "WHERE start.id IN $locationIds " +
                        "  AND length(path) <= $maxDepth " +
                        "WITH DISTINCT nearby " +
                        "MATCH (m:Movie)-[:FILMED_AT]->(nearby) " +
                        "WHERE m.id_imdb <> $excludeImdbId " +
                        "RETURN DISTINCT m.id_imdb as imdbId")
        List<String> findNearbyMoviesByBFS(@Param("locationIds") List<String> locationIds,
                        @Param("excludeImdbId") String excludeImdbId,
                        @Param("maxDepth") int maxDepth);

        interface LocationWithDistanceProjection {
                LocationNode getNeighbor();

                Double getDistance();
        }
}
