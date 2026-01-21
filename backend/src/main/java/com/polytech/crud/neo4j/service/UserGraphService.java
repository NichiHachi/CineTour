package com.polytech.crud.neo4j.service;

import com.polytech.crud.neo4j.entity.UserNode;
import com.polytech.crud.neo4j.entity.MovieNode;
import com.polytech.crud.neo4j.entity.MovieSearchRelationship;
import com.polytech.crud.neo4j.repository.UserNodeRepository;
import com.polytech.crud.neo4j.repository.MovieNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Profile;

@Service
@Profile("!import")
@RequiredArgsConstructor
@Transactional
public class UserGraphService {

    private final UserNodeRepository userNodeRepository;
    private final MovieNodeRepository movieNodeRepository;

    /**
     * Enregistre une recherche de film par un utilisateur
     */
    public void recordMovieSearch(String username, String movieTitle, String idImdb) {
        Optional<UserNode> userOpt = userNodeRepository.findByUsername(username);
        Optional<MovieNode> movieOpt = movieNodeRepository.findByIdImdb(idImdb);

        if (userOpt.isPresent() && movieOpt.isPresent()) {
            UserNode user = userOpt.get();
            MovieNode movie = movieOpt.get();

            MovieSearchRelationship search = new MovieSearchRelationship();
            search.setMovieTitle(movieTitle);
            search.setSearchTime(LocalDateTime.now());
            search.setIdImdb(idImdb);
            search.setMovie(movie);

            user.getSearchHistory().add(search);
            userNodeRepository.save(user);
        }
    }

    /**
     * Récupère l'historique de recherche d'un utilisateur
     */
    public Optional<UserNode> getUserWithSearchHistory(String username) {
        return userNodeRepository.findByUsernameWithSearchHistory(username);
    }

    /**
     * Crée un nouvel utilisateur
     */
    public UserNode createUser(String username, String password, String email) {
        UserNode user = new UserNode();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        return userNodeRepository.save(user);
    }
}
