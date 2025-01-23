package com.polytech.crud.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.polytech.crud.entity.Movie;
import com.polytech.crud.service.MovieService;
import com.polytech.crud.service.MovieSearchHistoryService;

@RestController
public class MovieController {

    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);

    @Autowired
    private MovieService service;

    @Autowired
    private MovieSearchHistoryService movieSearchHistoryService;

    /**
     * Method POST with a JSON body.
     * Add a movie to the database.
     *
     * @param movie JSON Body
     * @return
     */
    @PostMapping("/addMovie")
    public Movie addMovie(@RequestBody Movie movie) {
        return service.saveMovie(movie);
    }

    /**
     * Method POST with a JSON body.
     * Add a list of movies to the database.
     *
     * @param movies JSON Body
     * @return
     */
    @PostMapping("/addMovies")
    public List<Movie> addMovies(@RequestBody List<Movie> movies) {
        return service.saveMovies(movies);
    }

    /**
     * Method GET.
     * Get all movies from the database.
     *
     * @return
     */
    @GetMapping("/movies")
    public List<Movie> findAllMovies() {
        List<Movie> movies = service.getAllMovies();
        if (movies.isEmpty()) {
            logger.info("No movies found in database");
        }
        return movies;
    }

    /**
     * Method GET with a path variable.
     * Get a movie by ID from the database.
     *
     * @param id int
     * @return
     */
    @GetMapping("/movieById/{id}")
    public Movie findMovieById(@PathVariable int id) {
        Movie movie = service.getMovieById(id);
        if (movie == null) {
            logger.info("No movie found in database for ID: {}", id);
        }
        return movie;
    }

    /**
     * Method GET with a path variable.
     * Get a movie by IMDB ID from the database.
     *
     * @param id String
     * @return
     */
    @GetMapping("/movieByImdbId/{id}")
    public ResponseEntity<Movie> findMovieByImdbId(@PathVariable String id,
            @CookieValue(value = "username", defaultValue = "") String username) {
        Movie movie = service.getMovieByImdbId(id);
        if (movie == null) {
            logger.info("No movie found in database for ID: {}", id);
            return ResponseEntity.notFound().build();
        } else {
            movieSearchHistoryService.saveMovieSearchHistoryByImdbId(id, movie.getTitle(), username);
            return ResponseEntity.ok(movie);
        }
    }

    /**
     * Method GET with a path variable.
     * Get movies by title from the database.
     *
     * @param title String
     * @return
     */
    @GetMapping("/movie/{title}")
    public List<Movie> getMoviesByTitle(@PathVariable String title) {
        logger.info("Searching movies for title: {}", title);

        List<Movie> movies = service.getMoviesByTitle(title);
        if (movies.isEmpty()) {
            logger.info("No movies found for title: {}", title);
        }

        return movies;
    }

    /**
     * Search movies by title (case insensitive) ordered by popularity.
     *
     * @param title Search term
     * @return List of movies matching the search term, ordered by search counts
     */
    @GetMapping("/search")
    public List<Movie> searchMovies(@RequestParam String title) {
        logger.info("Searching movies with title: {}", title);
        return service.searchMoviesOrderByPopularity(title);
    }

    /**
     * Method PUT with a JSON body.
     * Update a movie in the database.
     *
     * @param movie JSON Body
     * @return ResponseEntity<Movie> A response entity containing the updated movie
     *         or an error message
     */
    @PutMapping("/updateMovie")
    public ResponseEntity<?> updateMovie(@RequestBody Movie movie) {
        try {
            Movie updatedMovie = service.updateMovie(movie);
            return ResponseEntity.ok(updatedMovie);
        } catch (Exception e) {
            logger.error("Failed to update movie: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update movie");
        }
    }

    /**
     * Method DELETE with a path variable.
     * Delete a movie from the database.
     *
     * @param id int
     * @return ResponseEntity<String> A response entity containing a success or
     *         error message
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id) {
        try {
            service.deleteMovieById(id);
            return ResponseEntity.ok("Movie deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete movie");
        }
    }
}
