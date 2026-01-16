package com.polytech.crud.repository;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.polytech.crud.entity.Movie;
import com.polytech.crud.entity.Person;

public interface PersonRepository extends Neo4jRepository<Person, Long> {
    @Query("MATCH (p:Person)-[:ACTED_IN]->(m:Movie) WHERE p.name = $name RETURN m")
    List<Movie> findMoviesActedByPerson(@Param("name") String name);

    @Query("MATCH (p:Person)-[:DIRECTED]->(m:Movie) WHERE p.name = $name RETURN m")
    List<Movie> findMoviesDirectedByPerson(@Param("name") String name);

    @Query("MATCH (p:Person)-[:ACTED_IN]->(m:Movie {title: $title}) RETURN p")
    List<Person> findActorsByMovie(@Param("title") String title);

    @Query("MATCH (p:Person)-[:DIRECTED]->(m:Movie {title: $title}) RETURN p")
    List<Person> findDirectorsByMovie(@Param("title") String title);

    @Query("MATCH (a:Person)-[:ACTED_IN]->(m1:Movie) MATCH (a)-[:ACTED_IN]->(m2:Movie) WHERE m1 <> m2 AND m1.title = $title RETURN DISTINCT m2")
    List<Movie> findMoviesWithSharedActors(@Param("title") String title);

    Person findByNconst(String Nconst);
}
