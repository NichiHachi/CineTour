package com.polytech.crud.neo4j.controller;

import com.polytech.crud.neo4j.entity.LocationNode;
import com.polytech.crud.neo4j.service.LocationGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Profile("!import")
@RequestMapping("/api/neo4j/locations/rng")
@RequiredArgsConstructor
public class LocationGraphController {

    private final LocationGraphService locationGraphService;

    @PostMapping("/build")
    public ResponseEntity<Integer> buildRNG() {
        return ResponseEntity.ok(locationGraphService.buildRelativeNeighborhoodGraph());
    }

    @GetMapping("/path")
    public ResponseEntity<List<LocationNode>> findShortestPath(
            @RequestParam Long from,
            @RequestParam Long to) {
        return ResponseEntity.ok(locationGraphService.findShortestPath(from, to));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<String>> getNearbyMovies(
            @RequestParam List<Long> locationIds,
            @RequestParam String excludeImdbId,
            @RequestParam(defaultValue = "5") int minMovies) {
        return ResponseEntity.ok(locationGraphService.getNearbyMovieImdbIds(
                locationIds, excludeImdbId, minMovies));
    }

    @PostMapping("/add/{locationId}")
    public ResponseEntity<Integer> addLocationToRNG(@PathVariable Long locationId) {
        return ResponseEntity.ok(locationGraphService.addLocationToRNG(locationId));
    }
}
