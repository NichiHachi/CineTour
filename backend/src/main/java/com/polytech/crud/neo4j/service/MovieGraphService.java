package com.polytech.crud.neo4j.service;

import com.polytech.crud.neo4j.entity.MovieNode;
import com.polytech.crud.neo4j.entity.LocationNode;
import com.polytech.crud.neo4j.entity.PersonNode;
import com.polytech.crud.neo4j.repository.MovieNodeRepository;
import com.polytech.crud.neo4j.repository.LocationNodeRepository;
import com.polytech.crud.neo4j.repository.PersonNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Optional;

@Service
@Profile("!import")
@RequiredArgsConstructor
@Transactional
public class MovieGraphService {

    private final MovieNodeRepository movieNodeRepository;
    private final LocationNodeRepository locationNodeRepository;
    private final PersonNodeRepository personNodeRepository;

    /**
     * Trouve un film par son ID IMDB avec toutes ses relations
     */
    public Optional<MovieNode> findMovieWithAllRelations(String idImdb) {
        return movieNodeRepository.findByIdImdb(idImdb);
    }

    /**
     * Trouve tous les films tournés dans un pays spécifique
     */
    public List<MovieNode> findMoviesByCountry(String countryCode) {
        // Cette requête nécessiterait une query personnalisée
        return movieNodeRepository.findAll().stream()
                .filter(movie -> movie.getLocations().stream()
                        .anyMatch(loc -> countryCode.equals(loc.getCountryCode())))
                .toList();
    }

    /**
     * Trouve tous les films réalisés par une personne
     */
    public List<MovieNode> findMoviesByDirector(String nconst) {
        return movieNodeRepository.findAll().stream()
                .filter(movie -> movie.getDirectors().stream()
                        .anyMatch(director -> nconst.equals(director.getNconst())))
                .toList();
    }

    /**
     * Trouve les lieux de tournage proches d'une position géographique
     */
    public List<LocationNode> findNearbyLocations(Double latitude, Double longitude, Double radiusInKm) {
        Double radiusInMeters = radiusInKm * 1000;
        return locationNodeRepository.findNearby(latitude, longitude, radiusInMeters);
    }

    /**
     * Sauvegarde un film avec ses relations
     */
    public MovieNode saveMovie(MovieNode movie) {
        return movieNodeRepository.save(movie);
    }

    /**
     * Recherche des films par titre
     */
    public List<MovieNode> searchMoviesByTitle(String title) {
        return movieNodeRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Trouve les réalisateurs d'un film
     */
    public List<PersonNode> findDirectorsOfMovie(String idImdb) {
        return personNodeRepository.findDirectorsByMovieIdImdb(idImdb);
    }
}
