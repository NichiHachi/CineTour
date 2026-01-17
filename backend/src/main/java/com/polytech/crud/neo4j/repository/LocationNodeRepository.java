package com.polytech.crud.neo4j.repository;

import com.polytech.crud.neo4j.entity.LocationNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
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
}
