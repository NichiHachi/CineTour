package com.polytech.crud.service;

import java.time.Year;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Transactional("transactionManager")
    public Movie getMovieByImdbId(String idImdb) {
        Movie movie = repository.findByIdImdb(idImdb);
        if (movie == null) {
            return null;
        }
        incrementMovieCountAsync(movie.getId());
        return movie;
    }

    /**
     * Incrémente le compteur de recherche de façon asynchrone.
     */
    @Async
    @Transactional("transactionManager")
    public void incrementMovieCountAsync(int movieId) {
        Movie movie = repository.findById(movieId).orElse(null);
        if (movie != null) {
            movie.setMovieSearchCount(movie.getMovieSearchCount() + 1);
            repository.save(movie);
            logger.debug("Incremented search count for movie {} to {}", movie.getIdImdb(), movie.getMovieSearchCount());
        }
    }

    @Transactional("transactionManager")
    public List<Movie> getMoviesByTitle(String title) {
        List<Movie> movies = repository.findByTitle(title);
        // Incrémenter les compteurs de façon asynchrone
        for (Movie movie : movies) {
            incrementMovieCountAsync(movie.getId());
        }
        return movies;
    }

    public Page<Movie> searchMoviesWithFilters(String title, Year fromYear, Year toYear,
                                               List<String> genres, Double minRating,
                                               Double maxRating, int page, int size) {
        
        String genre1 = (genres != null && genres.size() > 0) ? genres.get(0) : null;
        String genre2 = (genres != null && genres.size() > 1) ? genres.get(1) : null;
        String genre3 = (genres != null && genres.size() > 2) ? genres.get(2) : null;

        Integer fromYearInt = (fromYear != null) ? fromYear.getValue() : null;
        Integer toYearInt = (toYear != null) ? toYear.getValue() : null;

        Pageable pageable = PageRequest.of(page, size);

        return repository.searchMoviesWithFilters(
                title, fromYearInt, toYearInt, genre1, genre2, genre3, minRating, maxRating, pageable
        );
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