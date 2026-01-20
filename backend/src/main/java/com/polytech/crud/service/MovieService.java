package com.polytech.crud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polytech.crud.entity.Movie;
import com.polytech.crud.repository.MovieRepository;

@Service
public class MovieService {
    private static final Logger logger = LoggerFactory.getLogger(ImdbLocationsService.class);

    @Autowired
    private MovieRepository repository;

    @Autowired
    private ImdbMoviesService imdbMoviesService;

    public Movie saveMovie(Movie movie) {
        return repository.save(movie);
    }

    public List<Movie> saveMovies(List<Movie> movies) {
        return repository.saveAll(movies);
    }

    public List<Movie> getAllMovies() {
        return repository.findAll();
    }

    public Movie getMovieById(int id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public Movie getMovieByImdbId(String idImdb) {
        Movie movie = repository.findByIdImdb(idImdb);
        if (movie == null) {
            return null;
        }
        if (!Boolean.TRUE.equals(movie.getTmdbInfoChecked())) {
            imdbMoviesService.enrichMovieWithTmdbInfo(idImdb);
            movie = repository.findByIdImdb(idImdb);
        }
        incrementMovieCountAsync(movie.getId());
        return movie;
    }

    /**
     * Incrémente le compteur de recherche de façon asynchrone.
     */
    @Async
    @Transactional
    public void incrementMovieCountAsync(int movieId) {
        Movie movie = repository.findById(movieId).orElse(null);
        if (movie != null) {
            movie.setMovieSearchCount(movie.getMovieSearchCount() + 1);
            repository.save(movie);
            logger.debug("Incremented search count for movie {} to {}", movie.getIdImdb(), movie.getMovieSearchCount());
        }
    }

    public List<Movie> getMoviesByTitle(String title) {
        List<Movie> movies = repository.findByTitle(title);
        // Incrémenter les compteurs de façon asynchrone
        for (Movie movie : movies) {
            incrementMovieCountAsync(movie.getId());
        }
        return movies;
    }

    public List<Movie> searchMoviesOrderByPopularity(String query) {
        return repository.searchByTitleContainingOrderBySearchCount(query);
    }

    public String deleteMovieById(int id) {
        try {
            repository.deleteById(id);
            return "Movie removed with the id : " + id;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public Movie updateMovie(Movie movie) {
        Movie existingMovie = repository.findById(movie.getId()).orElse(null);
        assert existingMovie != null;
        existingMovie.setTitle(movie.getTitle());
        return repository.save(existingMovie);
    }
}