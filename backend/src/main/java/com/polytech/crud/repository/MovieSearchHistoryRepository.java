package com.polytech.crud.repository;

import com.polytech.crud.entity.MovieSearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieSearchHistoryRepository extends JpaRepository<MovieSearchHistory, Long> {
    List<MovieSearchHistory> findByUserId(Long userId);
    MovieSearchHistory findByIdImdb(String imdbId); // New method to find by IMDB ID
}
