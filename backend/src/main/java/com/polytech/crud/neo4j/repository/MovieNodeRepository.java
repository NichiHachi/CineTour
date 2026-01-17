package com.polytech.crud.neo4j.repository;

import com.polytech.crud.neo4j.entity.MovieNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieNodeRepository extends Neo4jRepository<MovieNode, Long> {

    Optional<MovieNode> findByIdImdb(String idImdb);

    List<MovieNode> findByTitleContainingIgnoreCase(String title);

    @Query("MATCH (m:Movie) WHERE m.releaseYear = $year RETURN m")
    List<MovieNode> findByReleaseYear(@Param("year") Integer year);

    @Query("MATCH (m:Movie)-[:FILMED_AT]->(l:Location) WHERE m.idImdb = $idImdb RETURN m, l")
    Optional<MovieNode> findByIdImdbWithLocations(@Param("idImdb") String idImdb);

    @Query("MATCH (m:Movie)-[:DIRECTED_BY]->(p:Person) WHERE m.idImdb = $idImdb RETURN m, p")
    Optional<MovieNode> findByIdImdbWithDirectors(@Param("idImdb") String idImdb);

    @Query("MATCH (m:Movie)-[r:HAS_PRINCIPAL]->(pr:Person) WHERE m.idImdb = $idImdb RETURN m, r, pr")
    Optional<MovieNode> findByIdImdbWithPrincipals(@Param("idImdb") String idImdb);
}
