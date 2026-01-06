package com.polytech.crud.repository;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import com.polytech.crud.entity.Movie;

public interface MovieRepository extends Neo4jRepository<Movie, Long> {
    List<Movie> findByTitle(String name);

    Movie findByIdImdb(String idImdb);

    @Query("MATCH (m:Movie) WHERE toLower(m.title) CONTAINS toLower($query) RETURN m ORDER BY (m.movieSearchCount + m.locationSearchCount) DESC LIMIT 10")
    List<Movie> searchByTitleContainingOrderBySearchCount(@Param("query") String query);
}
