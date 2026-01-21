package com.polytech.crud.neo4j.controller;

import com.polytech.crud.neo4j.entity.MovieNode;
import com.polytech.crud.neo4j.entity.LocationNode;
import com.polytech.crud.neo4j.entity.PersonNode;
import com.polytech.crud.neo4j.service.MovieGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.context.annotation.Profile;

@RestController
@Profile("!import")
@RequestMapping("/api/neo4j/movies")
@RequiredArgsConstructor
public class MovieGraphController {

    private final MovieGraphService movieGraphService;

    /**
     * Recherche de films par titre
     */
    @GetMapping("/search")
    public ResponseEntity<List<MovieNode>> searchMovies(@RequestParam String title) {
        List<MovieNode> movies = movieGraphService.searchMoviesByTitle(title);
        return ResponseEntity.ok(movies);
    }

    /**
     * Récupère un film par son ID IMDB avec toutes ses relations
     */
    @GetMapping("/imdb/{idImdb}")
    public ResponseEntity<MovieNode> getMovieByIdImdb(@PathVariable String idImdb) {
        return movieGraphService.findMovieWithAllRelations(idImdb)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupère les films tournés dans un pays
     */
    @GetMapping("/country/{countryCode}")
    public ResponseEntity<List<MovieNode>> getMoviesByCountry(@PathVariable String countryCode) {
        List<MovieNode> movies = movieGraphService.findMoviesByCountry(countryCode);
        return ResponseEntity.ok(movies);
    }

    /**
     * Récupère les films réalisés par une personne
     */
    @GetMapping("/director/{nconst}")
    public ResponseEntity<List<MovieNode>> getMoviesByDirector(@PathVariable String nconst) {
        List<MovieNode> movies = movieGraphService.findMoviesByDirector(nconst);
        return ResponseEntity.ok(movies);
    }

    /**
     * Récupère les réalisateurs d'un film
     */
    @GetMapping("/imdb/{idImdb}/directors")
    public ResponseEntity<List<PersonNode>> getDirectorsOfMovie(@PathVariable String idImdb) {
        List<PersonNode> directors = movieGraphService.findDirectorsOfMovie(idImdb);
        return ResponseEntity.ok(directors);
    }

    /**
     * Trouve les lieux de tournage proches d'une position
     */
    @GetMapping("/locations/nearby")
    public ResponseEntity<List<LocationNode>> getNearbyLocations(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {
        List<LocationNode> locations = movieGraphService.findNearbyLocations(latitude, longitude, radiusKm);
        return ResponseEntity.ok(locations);
    }

    /**
     * Sauvegarde un nouveau film
     */
    @PostMapping
    public ResponseEntity<MovieNode> createMovie(@RequestBody MovieNode movie) {
        MovieNode savedMovie = movieGraphService.saveMovie(movie);
        return ResponseEntity.ok(savedMovie);
    }
}
