package com.polytech.crud.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.polytech.crud.entity.Movie;
import com.polytech.crud.entity.Producer;

public interface ProducerRepository extends Neo4jRepository<Producer, Long> {
    Producer findByName(String name);

    @Query("MATCH (p:Producer)<-[:PRODUCED_BY]-(m:Movie) RETURN p.name AS Producer, collect(m.title) AS Movies")
    List<Map<String, Object>> findAllWithMovies();

    @Query("MATCH (p:Producer {name: $name})<-[:PRODUCED_BY]-(m:Movie) RETURN m")
    List<Movie> findMoviesByProducer(@Param("name") String name);
}
