package com.polytech.crud.neo4j.repository;

import com.polytech.crud.neo4j.entity.PersonNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonNodeRepository extends Neo4jRepository<PersonNode, Long> {

    Optional<PersonNode> findByNconst(String nconst);

    List<PersonNode> findByPrimaryNameContainingIgnoreCase(String name);

    @Query("MATCH (p:Person)<-[:DIRECTED_BY]-(m:Movie) WHERE p.nconst = $nconst RETURN p, m")
    Optional<PersonNode> findByNconstWithDirectedMovies(@Param("nconst") String nconst);

    @Query("MATCH (p:Person)-[:KNOWN_FOR]->(m:Movie) WHERE p.nconst = $nconst RETURN p, m")
    Optional<PersonNode> findByNconstWithKnownForMovies(@Param("nconst") String nconst);

    @Query("MATCH (m:Movie)-[:DIRECTED_BY]->(p:Person) WHERE m.idImdb = $idImdb RETURN p")
    List<PersonNode> findDirectorsByMovieIdImdb(@Param("idImdb") String idImdb);
}
