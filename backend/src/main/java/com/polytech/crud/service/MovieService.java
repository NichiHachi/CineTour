package com.polytech.crud.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polytech.crud.entity.Movie;
import com.polytech.crud.entity.Location;
import com.polytech.crud.entity.User;
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

    public Movie getMovieById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Movie getMovieByImdbId(String id) {
        return repository.findByIdImdb(id);
    }

    public List<Movie> getMoviesByTitle(String title) {
        List<Movie> movies = repository.findByTitle(title);
        incrementMovieCount(movies);
        return movies;
    }

    @Transactional
    private void incrementMovieCount(List<Movie> movies) {
        for (Movie movie : movies) {
            movie.setMovieSearchCount(movie.getMovieSearchCount() + 1);
            repository.save(movie);
            logger.debug("Incremented search locations count for movie {} to {}",
                    movie.getIdImdb(),
                    movie.getLocationSearchCount());
        }
    }

    public List<Movie> searchMoviesOrderByPopularity(String query) {
        return repository.searchByTitleContainingOrderBySearchCount(query);
    }

    public String deleteMovieById(Long id) {
        try {
            repository.deleteById(id);
            return "Movie removed with the id : " + id;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public Movie updateMovie(Movie movie) {
        Movie existingMovie = repository.findById(movie.getId()).orElse(null);
        existingMovie.setTitle(movie.getTitle());
        return repository.save(existingMovie);
    }

    // Méthodes de test pour afficher les données
    public Long countMovies() {
        return repository.countAllMovies();
    }

    public List<Movie> getMoviesByYear(Integer year) {
        return repository.findByYear(year);
    }

    public List<Movie> getRecentMovies(Integer limit) {
        return repository.findRecentMovies(limit);
    }

    public List<Movie> getMostPopularMovies(Integer limit) {
        return repository.findMostPopularMovies(limit);
    }

    public List<Movie> getMoviesByGenre(String genre) {
        return repository.findByGenre(genre);
    }

    public void displayMoviesInConsole() {
        List<Movie> movies = repository.findAll();
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📽️  FILMS DANS LA BASE DE DONNÉES NEO4J");
        System.out.println("=".repeat(60));
        System.out.println("Total: " + movies.size() + " films\n");

        movies.stream().limit(10).forEach(movie -> {
            System.out.println("🎬 " + movie.getTitle() + " (" + movie.getReleaseYear() + ")");
            System.out.println("   IMDB: " + movie.getIdImdb());
            System.out.println("   Genres: " + movie.getGenres());
            System.out.println("   Recherches: " + (movie.getMovieSearchCount() + movie.getLocationSearchCount()));
            System.out.println();
        });
        System.out.println("=".repeat(60) + "\n");
    }

    // Méthodes pour explorer les voisins dans le graphe
    public List<Map<String, Object>> getNeighborsSummary(String idImdb) {
        return repository.findNeighborsSummary(idImdb);
    }

    public List<Location> getLocationsByMovie(String idImdb) {
        return repository.findLocationsByMovie(idImdb);
    }

    public List<Movie> getMoviesByLocation(String location) {
        return repository.findMoviesByLocation(location);
    }

    public List<Movie> getMoviesSearchedByUser(String username) {
        return repository.findMoviesSearchedByUser(username);
    }

    public List<User> getUsersWhoSearchedMovie(String idImdb) {
        return repository.findUsersWhoSearchedMovie(idImdb);
    }

    public List<Movie> getMoviesWithSharedLocations(String idImdb) {
        return repository.findMoviesWithSharedLocations(idImdb);
    }

    public List<Map<String, Object>> getNeighborsAtDistance(String idImdb) {
        return repository.findNeighborsAtDistance(idImdb);
    }

    public void displayGraphNeighbors(String idImdb) {
        Movie movie = repository.findByIdImdb(idImdb);
        if (movie == null) {
            System.out.println("Film non trouvé : " + idImdb);
            return;
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("🔗 VOISINS DU FILM : " + movie.getTitle());
        System.out.println("=".repeat(70));

        // Résumé des voisins
        List<Map<String, Object>> summary = getNeighborsSummary(idImdb);
        System.out.println("\n📊 Résumé des relations :");
        summary.forEach(s -> {
            System.out.println("   - " + s.get("relationshipType") + " -> " +
                    s.get("nodeLabels") + " : " + s.get("count") + " nœud(s)");
        });

        // Lieux de tournage
        List<Location> locations = getLocationsByMovie(idImdb);
        System.out.println("\n📍 Lieux de tournage (" + locations.size() + ") :");
        locations.stream().limit(5).forEach(l -> System.out.println("   - " + l.getLocationString()));

        // Films similaires (lieux partagés)
        List<Movie> similarMovies = getMoviesWithSharedLocations(idImdb);
        System.out.println("\n🎬 Films tournés dans des lieux similaires (" + similarMovies.size() + ") :");
        similarMovies.stream().limit(5)
                .forEach(m -> System.out.println("   - " + m.getTitle() + " (" + m.getReleaseYear() + ")"));

        System.out.println("\n" + "=".repeat(70) + "\n");
    }
}
