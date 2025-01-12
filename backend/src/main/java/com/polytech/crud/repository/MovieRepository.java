package com.polytech.crud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.polytech.crud.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findByTitle(String name);
    Movie findByIdImdb(String idImdb);
}
