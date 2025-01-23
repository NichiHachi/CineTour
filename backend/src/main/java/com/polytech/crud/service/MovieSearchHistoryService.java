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

        MovieSearchHistory movieSearchHistory = new MovieSearchHistory();
        movieSearchHistory.setUser(user);
        movieSearchHistory.setMovieTitle(movieTitle);
        movieSearchHistory.setSearchTime(LocalDateTime.now());
        movieSearchHistory.setImdbId(imdbId);
        return repository.save(movieSearchHistory);
    }

    public List<MovieSearchHistory> getMovieSearchHistoryByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
}
