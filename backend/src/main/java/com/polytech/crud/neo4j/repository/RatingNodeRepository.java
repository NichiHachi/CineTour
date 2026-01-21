package com.polytech.crud.neo4j.repository;

import com.polytech.crud.neo4j.entity.RatingNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;

@Repository
@Profile("!import")
public interface RatingNodeRepository extends Neo4jRepository<RatingNode, Long> {

    Optional<RatingNode> findByIdImdb(String idImdb);

    @Query("MATCH (r:Rating) WHERE r.averageRating >= $minRating RETURN r ORDER BY r.averageRating DESC")
    List<RatingNode> findByMinimumRating(@Param("minRating") Double minRating);

    @Query("MATCH (m:Movie)-[:HAS_RATING]->(r:Rating) WHERE m.idImdb = $idImdb RETURN r")
    Optional<RatingNode> findByMovieIdImdb(@Param("idImdb") String idImdb);
}
