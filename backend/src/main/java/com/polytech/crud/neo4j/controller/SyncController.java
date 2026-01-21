package com.polytech.crud.neo4j.controller;

import com.polytech.crud.neo4j.sync.SqlToNeo4jSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;

/**
 * Contrôleur pour gérer la synchronisation entre SQL et Neo4j
 */
@RestController
@Profile("!import")
@RequestMapping("/api/neo4j/sync")
@RequiredArgsConstructor
@Slf4j
public class SyncController {

    private final SqlToNeo4jSyncService syncService;

    /**
     * Synchronisation complète de toutes les données SQL → Neo4j
     */
    @PostMapping("/full")
    public ResponseEntity<Map<String, String>> fullSync() {
        log.info("Démarrage de la synchronisation complète SQL → Neo4j");

        try {
            syncService.fullSync();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Synchronisation complète terminée avec succès");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation complète", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur : " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Synchronisation des films uniquement
     */
    @PostMapping("/movies")
    public ResponseEntity<Map<String, String>> syncMovies() {
        log.info("Démarrage de la synchronisation des films");

        try {
            syncService.syncAllMovies();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Synchronisation des films terminée");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation des films", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur : " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Synchronisation des lieux uniquement
     */
    @PostMapping("/locations")
    public ResponseEntity<Map<String, String>> syncLocations() {
        log.info("Démarrage de la synchronisation des lieux");

        try {
            syncService.syncAllLocations();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Synchronisation des lieux terminée");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation des lieux", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur : " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Synchronisation des personnes uniquement
     */
    @PostMapping("/persons")
    public ResponseEntity<Map<String, String>> syncPersons() {
        log.info("Démarrage de la synchronisation des personnes");

        try {
            syncService.syncAllPersons();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Synchronisation des personnes terminée");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation des personnes", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur : " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Synchronisation des utilisateurs uniquement
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, String>> syncUsers() {
        log.info("Démarrage de la synchronisation des utilisateurs");

        try {
            syncService.syncAllUsers();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Synchronisation des utilisateurs terminée");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation des utilisateurs", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur : " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Synchronisation des relations Film-Lieu
     */
    @PostMapping("/relations/movie-location")
    public ResponseEntity<Map<String, String>> syncMovieLocationRelations() {
        log.info("Démarrage de la synchronisation des relations Film-Lieu");

        try {
            syncService.syncMovieLocationRelations();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Synchronisation des relations Film-Lieu terminée");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation des relations Film-Lieu", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur : " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Synchronisation des relations Film-Réalisateur
     */
    @PostMapping("/relations/movie-director")
    public ResponseEntity<Map<String, String>> syncMovieDirectorRelations() {
        log.info("Démarrage de la synchronisation des relations Film-Réalisateur");

        try {
            syncService.syncMovieDirectorRelations();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Synchronisation des relations Film-Réalisateur terminée");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation des relations Film-Réalisateur", e);

            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur : " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }
}
