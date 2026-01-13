package com.polytech.crud.repository;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.polytech.crud.entity.Genre;
import com.polytech.crud.entity.Movie;

public interface GenreRepository extends Neo4jRepository<Genre, Long> {
    Genre findByName(String name);

    @Query("MATCH (g:Genre)<-[:HAS_GENRE]-(m:Movie) RETURN g, collect(m) ORDER BY g.name")
    List<Genre> findAllWithMovies();

    @Query("MATCH (g:Genre {name: $name})<-[:HAS_GENRE]-(m:Movie) RETURN m")
    List<Movie> findMoviesByGenre(@Param("name") String name);

    @Query("MATCH (m:Movie)-[:HAS_GENRE]->(g:Genre) RETURN g.name AS Genre, avg(m.reviewScore) AS AverageRating ORDER BY AverageRating DESC")
    List<Object> findAverageRatingByGenre();
}
