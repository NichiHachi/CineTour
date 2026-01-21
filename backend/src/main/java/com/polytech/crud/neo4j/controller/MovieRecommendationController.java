package com.polytech.crud.neo4j.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.polytech.crud.neo4j.entity.MovieNode;
import com.polytech.crud.neo4j.service.MovieRecommendationService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;

@RestController
@Profile("!import")
@RequestMapping("/api/neo4j/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MovieRecommendationController {

    private final MovieRecommendationService recommendationService;

    /**
     * Endpoint principal : recommandations intelligentes
     * Combine directeurs, acteurs et genres avec scoring avancé
     * 
     * @param imdbId    ID IMDB du film source
     * @param minRating Rating minimum (défaut: 6.0)
     * @param limit     Nombre maximum de résultats (défaut: 20)
     * @return Liste de films recommandés avec scores
     */
    @GetMapping("/relatedMovies/{imdbId}")
    public ResponseEntity<List<Map<String, Object>>> getRelatedMovies(
            @PathVariable String imdbId,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer limit) {

        List<Map<String, Object>> recommendations = recommendationService.getSmartRecommendations(imdbId, minRating,
                limit);

        return ResponseEntity.ok(recommendations);
    }

    /**
     * Recommandations détaillées avec raisons
     * 
     * @param imdbId    ID IMDB du film source
     * @param minRating Rating minimum (défaut: 6.0)
     * @param limit     Nombre maximum de résultats (défaut: 20)
     * @return Liste de films avec raisons de recommandation
     */
    @GetMapping("/relatedMovies/{imdbId}/detailed")
    public ResponseEntity<List<Map<String, Object>>> getDetailedRecommendations(
            @PathVariable String imdbId,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer limit) {

        List<Map<String, Object>> recommendations = recommendationService.getDetailedRecommendations(imdbId, minRating,
                limit);

        return ResponseEntity.ok(recommendations);
    }

    /**
     * Retourne uniquement les films (objets MovieNode complets)
     * 
     * @param imdbId    ID IMDB du film source
     * @param minRating Rating minimum (défaut: 6.0)
     * @param limit     Nombre maximum de résultats (défaut: 20)
     * @return Liste de films recommandés
     */
    @GetMapping("/relatedMovies/{imdbId}/movies")
    public ResponseEntity<List<MovieNode>> getRelatedMoviesOnly(
            @PathVariable String imdbId,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer limit) {

        List<MovieNode> movies = recommendationService.getRecommendedMovies(imdbId, minRating, limit);
        return ResponseEntity.ok(movies);
    }

    /**
     * Retourne uniquement les IDs IMDB des films recommandés
     * 
     * @param imdbId    ID IMDB du film source
     * @param minRating Rating minimum (défaut: 6.0)
     * @param limit     Nombre maximum de résultats (défaut: 20)
     * @return Liste d'IDs IMDB
     */
    @GetMapping("/relatedMovies/{imdbId}/ids")
    public ResponseEntity<List<String>> getRelatedMovieIds(
            @PathVariable String imdbId,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer limit) {

        List<String> ids = recommendationService.getRecommendedMovieIds(imdbId, minRating, limit);
        return ResponseEntity.ok(ids);
    }

    /**
     * Recommandations basées uniquement sur les directeurs
     */
    @GetMapping("/relatedMoviesByDirectors/{imdbId}")
    public ResponseEntity<List<MovieNode>> getRecommendationsByDirectors(
            @PathVariable String imdbId,
            @RequestParam(defaultValue = "10") int limit) {

        List<MovieNode> movies = recommendationService.getRecommendationsByDirectors(imdbId, limit);
        return ResponseEntity.ok(movies);
    }

    /**
     * Recommandations basées uniquement sur les acteurs
     */
    @GetMapping("/relatedMoviesByActors/{imdbId}")
    public ResponseEntity<List<MovieNode>> getRecommendationsByActors(
            @PathVariable String imdbId,
            @RequestParam(defaultValue = "10") int limit) {

        List<MovieNode> movies = recommendationService.getRecommendationsByActors(imdbId, limit);
        return ResponseEntity.ok(movies);
    }

    /**
     * Recommandations basées uniquement sur le genre
     */
    @GetMapping("/relatedMoviesByGenre/{imdbId}")
    public ResponseEntity<List<MovieNode>> getRecommendationsByGenre(
            @PathVariable String imdbId,
            @RequestParam(defaultValue = "6.0") double minRating,
            @RequestParam(defaultValue = "10") int limit) {

        List<MovieNode> movies = recommendationService.getRecommendationsByGenre(imdbId, minRating, limit);
        return ResponseEntity.ok(movies);
    }

    /**
     * Recommandations basées sur l'époque et le genre
     */
    @GetMapping("/relatedMoviesByEra/{imdbId}")
    public ResponseEntity<List<MovieNode>> getRecommendationsByEraAndGenre(
            @PathVariable String imdbId,
            @RequestParam(defaultValue = "5") int yearRange,
            @RequestParam(defaultValue = "6.0") double minRating,
            @RequestParam(defaultValue = "10") int limit) {

        List<MovieNode> movies = recommendationService.getRecommendationsByEraAndGenre(
                imdbId, yearRange, minRating, limit);
        return ResponseEntity.ok(movies);
    }
}
