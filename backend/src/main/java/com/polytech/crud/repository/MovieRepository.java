package com.polytech.crud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.polytech.crud.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findByTitle(String name);

    Movie findByIdImdb(String idImdb);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY (m.movieSearchCount + m.locationSearchCount) DESC LIMIT 10")
    List<Movie> searchByTitleContainingOrderBySearchCount(@Param("query") String query);
}
