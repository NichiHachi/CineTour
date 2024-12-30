package com.polytech.crud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.polytech.crud.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    Movie findByTitle(String name);
}
