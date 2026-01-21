package com.polytech.crud.neo4j.controller;

import com.polytech.crud.neo4j.entity.UserNode;
import com.polytech.crud.neo4j.service.UserGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.context.annotation.Profile;

@RestController
@Profile("!import")
@RequestMapping("/api/neo4j/users")
@RequiredArgsConstructor
public class UserGraphController {

    private final UserGraphService userGraphService;

    /**
     * Crée un nouvel utilisateur
     */
    @PostMapping
    public ResponseEntity<UserNode> createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email) {
        UserNode user = userGraphService.createUser(username, password, email);
        return ResponseEntity.ok(user);
    }

    /**
     * Enregistre une recherche de film
     */
    @PostMapping("/{username}/search")
    public ResponseEntity<Void> recordSearch(
            @PathVariable String username,
            @RequestParam String movieTitle,
            @RequestParam String idImdb) {
        userGraphService.recordMovieSearch(username, movieTitle, idImdb);
        return ResponseEntity.ok().build();
    }

    /**
     * Récupère l'historique de recherche d'un utilisateur
     */
    @GetMapping("/{username}/history")
    public ResponseEntity<UserNode> getUserHistory(@PathVariable String username) {
        return userGraphService.getUserWithSearchHistory(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
