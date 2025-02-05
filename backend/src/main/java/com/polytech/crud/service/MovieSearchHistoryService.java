package com.polytech.crud.service;

import com.polytech.crud.entity.MovieSearchHistory;
import com.polytech.crud.entity.User;
import com.polytech.crud.repository.MovieSearchHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovieSearchHistoryService {
    @Autowired
    private MovieSearchHistoryRepository repository;

    @Autowired
    private UserService userService;

    public MovieSearchHistory saveMovieSearchHistoryByImdbId(String imdbId, String movieTitle, String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return null;
        }

        if (repository.findByIdImdb(imdbId) != null) {
            MovieSearchHistory existingMovieSearchHistory = repository.findByIdImdb(imdbId);
            existingMovieSearchHistory.setSearchTime(LocalDateTime.now());
            return repository.save(existingMovieSearchHistory);
        } else {
            // Create new entry
            MovieSearchHistory newMovieSearchHistory = new MovieSearchHistory();
            newMovieSearchHistory.setUser(user);
            newMovieSearchHistory.setMovieTitle(movieTitle);
            newMovieSearchHistory.setSearchTime(LocalDateTime.now());
            newMovieSearchHistory.setIdImdb(imdbId);
            return repository.save(newMovieSearchHistory);
        }

    }

    public List<MovieSearchHistory> getMovieSearchHistoryByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
}
