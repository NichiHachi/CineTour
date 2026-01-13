package com.polytech.crud.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.polytech.crud.entity.Genre;
import com.polytech.crud.entity.Movie;
import com.polytech.crud.entity.Person;
import com.polytech.crud.entity.Producer;
import com.polytech.crud.projection.MovieGenreCount;
import com.polytech.crud.repository.GenreRepository;
import com.polytech.crud.repository.MovieRepository;
import com.polytech.crud.repository.PersonRepository;
import com.polytech.crud.repository.ProducerRepository;

@RestController
@RequestMapping("/api")
public class GraphController {

    private static final Logger logger = LoggerFactory.getLogger(GraphController.class);

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ProducerRepository producerRepository;

    /**
     * Get movies that share actors with a given movie
     * 
     * @param title Movie title
     * @return List of movies with shared actors
     */
    @GetMapping("/movies/{title}/shared-actors")
    public ResponseEntity<List<Map<String, Object>>> getMoviesWithSharedActors(@PathVariable String title) {
        logger.info("Fetching movies with shared actors for: {}", title);
        List<Map<String, Object>> result = movieRepository.findMoviesWithSharedActorsByTitle(title);

        System.out.println("\n🤝 Films partageant des acteurs avec " + title + " :");
        result.forEach(
                r -> System.out.println("   - " + r.get("Movie2") + " (acteurs: " + r.get("SharedActors") + ")"));

        return ResponseEntity.ok(result);
    }

    /**
     * Get genres for a specific movie
     * 
     * @param title Movie title
     * @return List of genres
     */
    @GetMapping("/movies/{title}/genres")
    public ResponseEntity<List<String>> getGenresByMovie(@PathVariable String title) {
        logger.info("Fetching genres for movie: {}", title);
        List<String> genres = movieRepository.findGenresByMovieTitle(title);

        System.out.println("\n🎭 Genres du film " + title + " :");
        genres.forEach(g -> System.out.println("   - " + g));

        return ResponseEntity.ok(genres);
    }

    /**
     * Get actors for a specific movie
     * 
     * @param title Movie title
     * @return List of actors
     */
    @GetMapping("/movies/{title}/actors")
    public ResponseEntity<List<String>> getActorsByMovie(@PathVariable String title) {
        logger.info("Fetching actors for movie: {}", title);
        List<String> actors = movieRepository.findActorsByMovieTitle(title);

        System.out.println("\n🎬 Acteurs du film " + title + " :");
        actors.forEach(a -> System.out.println("   - " + a));

        return ResponseEntity.ok(actors);
    }

    /**
     * Get directors for a specific movie
     * 
     * @param title Movie title
     * @return List of directors
     */
    @GetMapping("/movies/{title}/directors")
    public ResponseEntity<List<String>> getDirectorsByMovie(@PathVariable String title) {
        logger.info("Fetching directors for movie: {}", title);
        List<String> directors = movieRepository.findDirectorsByMovieTitle(title);

        System.out.println("\n🎥 Réalisateurs du film " + title + " :");
        directors.forEach(d -> System.out.println("   - " + d));

        return ResponseEntity.ok(directors);
    }

    /**
     * Get producers for a specific movie
     * 
     * @param title Movie title
     * @return List of producers
     */
    @GetMapping("/movies/{title}/producers")
    public ResponseEntity<List<String>> getProducersByMovie(@PathVariable String title) {
        logger.info("Fetching producers for movie: {}", title);
        List<String> producers = movieRepository.findProducersByMovieTitle(title);

        System.out.println("\n🏢 Producteurs du film " + title + " :");
        producers.forEach(p -> System.out.println("   - " + p));

        return ResponseEntity.ok(producers);
    }

    /**
     * Get filming/narrative locations for a specific movie
     * 
     * @param title Movie title
     * @return List of locations with coordinates
     */
    @GetMapping("/movies/{title}/locations-full")
    public ResponseEntity<List<Map<String, Object>>> getLocationsByMovie(@PathVariable String title) {
        logger.info("Fetching locations for movie: {}", title);
        List<Map<String, Object>> locations = movieRepository.findLocationsByMovieTitle(title);

        System.out.println("\n📍 Lieux du film " + title + " :");
        locations.forEach(l -> System.out.println("   - " + l.get("name") +
                " (lon: " + l.get("longitude") + ", lat: " + l.get("latitude") + ")"));

        return ResponseEntity.ok(locations);
    }

    /**
     * Get all genres
     * 
     * @return List of all genres
     */
    @GetMapping("/genres")
    public ResponseEntity<List<Genre>> getAllGenres() {
        logger.info("Fetching all genres");
        List<Genre> genres = genreRepository.findAll();

        System.out.println("\n🎭 Tous les genres :");
        genres.forEach(g -> System.out.println("   - " + g.getName()));

        return ResponseEntity.ok(genres);
    }

    /**
     * Get movies by genre
     * 
     * @param name Genre name
     * @return List of movies
     */
    @GetMapping("/genres/{name}/movies")
    public ResponseEntity<List<Movie>> getMoviesByGenre(@PathVariable String name) {
        logger.info("Fetching movies for genre: {}", name);
        List<Movie> movies = genreRepository.findMoviesByGenre(name);

        System.out.println("\n🎬 Films du genre " + name + " :");
        movies.forEach(m -> System.out.println("   - " + m.getTitle()));

        return ResponseEntity.ok(movies);
    }

    /**
     * Get average rating by genre
     * 
     * @return Average ratings by genre
     */
    @GetMapping("/genres/ratings")
    public ResponseEntity<List<Map<String, Object>>> getAverageRatingByGenre() {
        logger.info("Fetching average ratings by genre");
        List<Map<String, Object>> ratings = movieRepository.findAverageRatingByGenre();

        System.out.println("\n⭐ Note moyenne par genre :");
        ratings.forEach(r -> System.out.println("   - " + r.get("Genre") + ": " + r.get("AverageRating")));

        return ResponseEntity.ok(ratings);
    }

    /**
     * Get all producers with their movies
     * 
     * @return List of producers with movies
     */
    @GetMapping("/producers")
    public ResponseEntity<List<Map<String, Object>>> getAllProducersWithMovies() {
        logger.info("Fetching all producers with movies");
        List<Map<String, Object>> producers = producerRepository.findAllWithMovies();

        System.out.println("\n🏢 Producteurs et leurs films :");
        producers.forEach(p -> System.out.println("   - " + p.get("Producer") + ": " + p.get("Movies")));

        return ResponseEntity.ok(producers);
    }

    /**
     * Get movies by producer
     * 
     * @param name Producer name
     * @return List of movies
     */
    @GetMapping("/producers/{name}/movies")
    public ResponseEntity<List<Movie>> getMoviesByProducer(@PathVariable String name) {
        logger.info("Fetching movies for producer: {}", name);
        List<Movie> movies = producerRepository.findMoviesByProducer(name);

        System.out.println("\n🎬 Films produits par " + name + " :");
        movies.forEach(m -> System.out.println("   - " + m.getTitle()));

        return ResponseEntity.ok(movies);
    }

    /**
     * Get movies acted by a person
     * 
     * @param name Person name
     * @return List of movies
     */
    @GetMapping("/actors/{name}/movies")
    public ResponseEntity<List<Movie>> getMoviesActedByPerson(@PathVariable String name) {
        logger.info("Fetching movies acted by: {}", name);
        List<Movie> movies = personRepository.findMoviesActedByPerson(name);

        System.out.println("\n🎬 Films avec " + name + " (acteur) :");
        movies.forEach(m -> System.out.println("   - " + m.getTitle()));

        return ResponseEntity.ok(movies);
    }

    /**
     * Get movies directed by a person
     * 
     * @param name Person name
     * @return List of movies
     */
    @GetMapping("/directors/{name}/movies")
    public ResponseEntity<List<Movie>> getMoviesDirectedByPerson(@PathVariable String name) {
        logger.info("Fetching movies directed by: {}", name);
        List<Movie> movies = personRepository.findMoviesDirectedByPerson(name);

        System.out.println("\n🎥 Films réalisés par " + name + " :");
        movies.forEach(m -> System.out.println("   - " + m.getTitle()));

        return ResponseEntity.ok(movies);
    }

    /**
     * Get movies with shared actors
     * 
     * @param title Movie title
     * @return List of movies with shared actors
     */
    @GetMapping("/movies/{title}/similar-by-actors")
    public ResponseEntity<List<Movie>> getMoviesWithSharedActorsByMovie(@PathVariable String title) {
        logger.info("Fetching movies with shared actors for: {}", title);
        List<Movie> movies = personRepository.findMoviesWithSharedActors(title);

        System.out.println("\n🤝 Films similaires (acteurs en commun) :");
        movies.forEach(m -> System.out.println("   - " + m.getTitle()));

        return ResponseEntity.ok(movies);
    }

    // ========== NOUVEAUX ENDPOINTS - EXPLORATION PAR GENRE ==========

    /**
     * Get movies that share genres with a given movie
     * 
     * @param title Movie title
     * @return List of movies with shared genres
     */
    @GetMapping("/movies/{title}/similar-by-genre")
    public ResponseEntity<List<Map<String, Object>>> getMoviesWithSharedGenres(@PathVariable String title) {
        logger.info("Fetching movies with shared genres for: {}", title);
        List<MovieGenreCount> results = movieRepository.findMoviesWithSharedGenres(title);

        List<Map<String, Object>> movies = results.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title", r.getTitle());
                    map.put("genreCount", r.getGenreCount());
                    return map;
                })
                .collect(Collectors.toList());

        System.out.println("\n🎭 Films partageant des genres avec " + title + " :");
        movies.forEach(
                m -> System.out.println("   - " + m.get("title") + " (" + m.get("genreCount") + " genres partagés)"));

        return ResponseEntity.ok(movies);
    }

    /**
     * Get summary of shared genres for a movie
     * 
     * @param title Movie title
     * @return Summary of genres and related movies count
     */
    @GetMapping("/movies/{title}/genre-connections")
    public ResponseEntity<List<Map<String, Object>>> getSharedGenresSummary(@PathVariable String title) {
        logger.info("Fetching shared genres summary for: {}", title);
        List<Map<String, Object>> summary = movieRepository.findSharedGenresSummary(title);

        System.out.println("\n📊 Résumé des genres partagés pour " + title + " :");
        summary.forEach(s -> System.out.println("   - " + s.get("genre") + ": " + s.get("movieCount") + " films"));

        return ResponseEntity.ok(summary);
    }
}
