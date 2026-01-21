package com.polytech.crud.neo4j.controller;

import com.polytech.crud.neo4j.service.AdvancedGraphQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;

/**
 * Contrôleur pour les requêtes avancées sur le graphe Neo4j
 */
@RestController
@Profile("!import")
@RequestMapping("/api/neo4j/advanced")
@RequiredArgsConstructor
public class AdvancedGraphController {

    private final AdvancedGraphQueryService queryService;

    /**
     * Trouve les films ayant des acteurs en commun
     * GET /api/neo4j/advanced/similar-movies/tt0111161?minActors=3
     */
    @GetMapping("/similar-movies/{idImdb}")
    public ResponseEntity<List<Map<String, Object>>> findSimilarMovies(
            @PathVariable String idImdb,
            @RequestParam(defaultValue = "2") int minActors) {
        List<Map<String, Object>> results = queryService.findMoviesWithCommonActors(idImdb, minActors);
        return ResponseEntity.ok(results);
    }

    /**
     * Trouve le chemin le plus court entre deux acteurs
     * GET /api/neo4j/advanced/actor-path?actor1=nm0000136&actor2=nm0000209
     */
    @GetMapping("/actor-path")
    public ResponseEntity<List<Map<String, Object>>> findActorPath(
            @RequestParam String actor1,
            @RequestParam String actor2) {
        List<Map<String, Object>> results = queryService.findShortestPathBetweenActors(actor1, actor2);
        return ResponseEntity.ok(results);
    }

    /**
     * Trouve les collaborations fréquentes entre acteurs
     * GET /api/neo4j/advanced/collaborations?minMovies=3
     */
    @GetMapping("/collaborations")
    public ResponseEntity<List<Map<String, Object>>> findCollaborations(
            @RequestParam(defaultValue = "3") int minMovies) {
        List<Map<String, Object>> results = queryService.findFrequentCollaborations(minMovies);
        return ResponseEntity.ok(results);
    }

    /**
     * Recommandations basées sur les lieux de tournage
     * GET /api/neo4j/advanced/recommend-by-location/tt0111161?country=US
     */
    @GetMapping("/recommend-by-location/{idImdb}")
    public ResponseEntity<List<Map<String, Object>>> recommendByLocation(
            @PathVariable String idImdb,
            @RequestParam String country) {
        List<Map<String, Object>> results = queryService.recommendMoviesByLocation(idImdb, country);
        return ResponseEntity.ok(results);
    }

    /**
     * Analyse la carrière d'un réalisateur
     * GET /api/neo4j/advanced/director-career/nm0001104
     */
    @GetMapping("/director-career/{nconst}")
    public ResponseEntity<List<Map<String, Object>>> analyzeDirectorCareer(@PathVariable String nconst) {
        List<Map<String, Object>> results = queryService.analyzeDirectorCareer(nconst);
        return ResponseEntity.ok(results);
    }

    /**
     * Trouve les lieux de tournage populaires
     * GET /api/neo4j/advanced/popular-locations?minMovies=5
     */
    @GetMapping("/popular-locations")
    public ResponseEntity<List<Map<String, Object>>> findPopularLocations(
            @RequestParam(defaultValue = "5") int minMovies) {
        List<Map<String, Object>> results = queryService.findPopularFilmingLocations(minMovies);
        return ResponseEntity.ok(results);
    }

    /**
     * Analyse les tendances de genres par décennie
     * GET /api/neo4j/advanced/genre-trends
     */
    @GetMapping("/genre-trends")
    public ResponseEntity<List<Map<String, Object>>> analyzeGenreTrends() {
        List<Map<String, Object>> results = queryService.analyzeGenreTrends();
        return ResponseEntity.ok(results);
    }

    /**
     * Trouve les personnes les plus connectées
     * GET /api/neo4j/advanced/most-connected?limit=20
     */
    @GetMapping("/most-connected")
    public ResponseEntity<List<Map<String, Object>>> findMostConnected(
            @RequestParam(defaultValue = "20") int limit) {
        List<Map<String, Object>> results = queryService.findMostConnectedPeople(limit);
        return ResponseEntity.ok(results);
    }
}
