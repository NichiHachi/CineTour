package com.polytech.crud.neo4j.sync;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polytech.crud.entity.Location;
import com.polytech.crud.entity.Movie;
import com.polytech.crud.entity.Person;
import com.polytech.crud.entity.Principal;
import com.polytech.crud.entity.Rating;
import com.polytech.crud.entity.User;
import com.polytech.crud.neo4j.entity.LocationNode;
import com.polytech.crud.neo4j.entity.MovieNode;
import com.polytech.crud.neo4j.entity.PersonNode;
import com.polytech.crud.neo4j.entity.PrincipalRelationship;
import com.polytech.crud.neo4j.entity.RatingNode;
import com.polytech.crud.neo4j.entity.UserNode;
import com.polytech.crud.neo4j.repository.LocationNodeRepository;
import com.polytech.crud.neo4j.repository.MovieNodeRepository;
import com.polytech.crud.neo4j.repository.PersonNodeRepository;
import com.polytech.crud.neo4j.repository.RatingNodeRepository;
import com.polytech.crud.neo4j.repository.UserNodeRepository;
import com.polytech.crud.repository.LocationRepository;
import com.polytech.crud.repository.MovieRepository;
import com.polytech.crud.repository.PersonRepository;
import com.polytech.crud.repository.PrincipalRepository;
import com.polytech.crud.repository.RatingRepository;
import com.polytech.crud.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service pour synchroniser les données entre MySQL et Neo4j
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SqlToNeo4jSyncService {

    private final MovieRepository sqlMovieRepo;
    private final LocationRepository sqlLocationRepo;
    private final PersonRepository sqlPersonRepo;
    private final RatingRepository sqlRatingRepo;
    private final PrincipalRepository sqlPrincipalRepo;
    private final UserRepository sqlUserRepo;

    private final MovieNodeRepository neo4jMovieRepo;
    private final LocationNodeRepository neo4jLocationRepo;
    private final PersonNodeRepository neo4jPersonRepo;
    private final RatingNodeRepository neo4jRatingRepo;
    private final UserNodeRepository neo4jUserRepo;

    /**
     * Synchronisation complète de toutes les données SQL → Neo4j
     */
    @Transactional
    public void fullSync() {
        log.info("Début de la synchronisation complète SQL → Neo4j");

        // Nettoyer Neo4j
        log.info("Nettoyage de Neo4j...");
        neo4jMovieRepo.deleteAll();
        neo4jLocationRepo.deleteAll();
        neo4jPersonRepo.deleteAll();
        neo4jRatingRepo.deleteAll();
        neo4jUserRepo.deleteAll();

        // Synchroniser les données
        syncAllMovies();
        syncAllPersons();
        syncAllLocations();
        syncAllRatings();
        syncAllPrincipals();
        syncAllUsers();

        log.info("Synchronisation complète terminée");
    }

    /**
     * Synchronise tous les films
     */
    @Transactional
    public void syncAllMovies() {
        log.info("Synchronisation des films...");
        List<Movie> movies = sqlMovieRepo.findAll();

        int count = 0;
        for (Movie movie : movies) {
            syncMovie(movie);
            count++;
            if (count % 100 == 0) {
                log.info(" {} films synchronisés...", count);
            }
        }

        log.info("✓ {} films synchronisés", count);
    }

    /**
     * Synchronise un film unique
     */
    @Transactional
    public MovieNode syncMovie(Movie sqlMovie) {
        MovieNode movieNode = new MovieNode();
        movieNode.setIdImdb(sqlMovie.getIdImdb());
        movieNode.setTitle(sqlMovie.getTitle());
        movieNode.setReleaseYear(sqlMovie.getReleaseYear());
        movieNode.setRuntimeMinutes(sqlMovie.getRuntimeMinutes());
        movieNode.setGenres(sqlMovie.getGenres());
        movieNode.setPosterPath(sqlMovie.getPosterPath());
        movieNode.setBackdropPath(sqlMovie.getBackdropPath());
        movieNode.setOverview(sqlMovie.getOverview());
        movieNode.setLocationsChecked(sqlMovie.getLocationsChecked());
        movieNode.setLocationSearchCount(sqlMovie.getLocationSearchCount());
        movieNode.setMovieSearchCount(sqlMovie.getMovieSearchCount());

        return neo4jMovieRepo.save(movieNode);
    }

    /**
     * Synchronise toutes les personnes
     */
    @Transactional
    public void syncAllPersons() {
        log.info("Synchronisation des personnes...");
        List<Person> persons = sqlPersonRepo.findAll();

        int count = 0;
        for (Person person : persons) {
            syncPerson(person);
            count++;
            if (count % 100 == 0) {
                log.info("  {} personnes synchronisées...", count);
            }
        }

        log.info(" {} personnes synchronisées", count);
    }

    /**
     * Synchronise une personne unique
     */
    @Transactional
    public PersonNode syncPerson(Person sqlPerson) {
        PersonNode personNode = new PersonNode();
        personNode.setNconst(sqlPerson.getNconst());
        personNode.setPrimaryName(sqlPerson.getPrimaryName());

        if (sqlPerson.getBirthYear() != null) {
            personNode.setBirthYear(sqlPerson.getBirthYear().getValue());
        }

        if (sqlPerson.getDeathYear() != null) {
            personNode.setDeathYear(sqlPerson.getDeathYear().getValue());
        }

        return neo4jPersonRepo.save(personNode);
    }

    /**
     * Synchronise tous les lieux
     */
    @Transactional
    public void syncAllLocations() {
        log.info("Synchronisation des lieux...");
        List<Location> locations = sqlLocationRepo.findAll();

        int count = 0;
        for (Location location : locations) {
            syncLocation(location);
            count++;
            if (count % 100 == 0) {
                log.info(" {} lieux synchronisés...", count);
            }
        }

        log.info(" {} lieux synchronisés avec relations", count);
    }

    /**
     * Synchronise un lieu unique et crée la relation FILMED_AT
     */
    @Transactional
    public LocationNode syncLocation(Location sqlLocation) {
        LocationNode locationNode = new LocationNode();
        locationNode.setIdImdb(sqlLocation.getIdImdb());
        locationNode.setLocationString(sqlLocation.getLocationString());
        locationNode.setDescription(sqlLocation.getDescription());
        locationNode.setLatitude(sqlLocation.getLatitude());
        locationNode.setLongitude(sqlLocation.getLongitude());
        locationNode.setDisplayName(sqlLocation.getDisplayName());
        locationNode.setCountryCode(sqlLocation.getCountryCode());
        locationNode.setGeocodingFailed(sqlLocation.getGeocodingFailed());

        LocationNode savedLocation = neo4jLocationRepo.save(locationNode);

        // Créer la relation FILMED_AT
        Optional<MovieNode> movieOpt = neo4jMovieRepo.findByIdImdb(sqlLocation.getIdImdb());
        if (movieOpt.isPresent()) {
            MovieNode movie = movieOpt.get();
            if (movie.getLocations() == null) {
                movie.setLocations(new HashSet<>());
            }
            movie.getLocations().add(savedLocation);
            neo4jMovieRepo.save(movie);
        }

        return savedLocation;
    }

    /**
     * Synchronise tous les ratings
     */
    @Transactional
    public void syncAllRatings() {
        log.info("Synchronisation des ratings...");
        List<Rating> ratings = sqlRatingRepo.findAll();

        int count = 0;
        for (Rating rating : ratings) {
            syncRating(rating);
            count++;
            if (count % 100 == 0) {
                log.info("  {} ratings synchronisés...", count);
            }
        }

        log.info("✓ {} ratings synchronisés avec relations", count);
    }

    /**
     * Synchronise un rating unique et crée la relation HAS_RATING
     */
    @Transactional
    public RatingNode syncRating(Rating sqlRating) {
        RatingNode ratingNode = new RatingNode();
        ratingNode.setAverageRating(sqlRating.getAverageRating());
        ratingNode.setNumVotes(sqlRating.getNumVotes());

        RatingNode savedRating = neo4jRatingRepo.save(ratingNode);

        // Créer la relation HAS_RATING
        Optional<MovieNode> movieOpt = neo4jMovieRepo.findByIdImdb(sqlRating.getIdImdb());
        if (movieOpt.isPresent()) {
            MovieNode movie = movieOpt.get();
            movie.setRating(savedRating);
            neo4jMovieRepo.save(movie);
        }

        return savedRating;
    }

    /**
     * Synchronise tous les principals (acteurs, réalisateurs, etc.)
     */
    @Transactional
    public void syncAllPrincipals() {
        log.info("🎭 Synchronisation des principals...");
        List<Principal> principals = sqlPrincipalRepo.findAll();

        int count = 0;
        for (Principal principal : principals) {
            syncPrincipal(principal);
            count++;
            if (count % 100 == 0) {
                log.info(" {} principals synchronisés...", count);
            }
        }

        log.info(" {} principals synchronisés avec relations", count);
    }

    /**
     * Synchronise un principal unique et crée les relations appropriées
     */
    @Transactional
    public void syncPrincipal(Principal sqlPrincipal) {
        Optional<MovieNode> movieOpt = neo4jMovieRepo.findByIdImdb(sqlPrincipal.getIdImdb());
        Optional<PersonNode> personOpt = neo4jPersonRepo.findByNconst(sqlPrincipal.getNconst());

        if (movieOpt.isEmpty() || personOpt.isEmpty()) {
            return;
        }

        MovieNode movie = movieOpt.get();
        PersonNode person = personOpt.get();

        // Créer la relation appropriée selon la catégorie
        if ("director".equalsIgnoreCase(sqlPrincipal.getCategory())) {
            if (movie.getDirectors() == null) {
                movie.setDirectors(new HashSet<>());
            }
            movie.getDirectors().add(person);
        } else {
            // Pour les acteurs et autres
            PrincipalRelationship relationship = new PrincipalRelationship();
            relationship.setOrdering(sqlPrincipal.getOrdering());
            relationship.setCategory(sqlPrincipal.getCategory());
            relationship.setJob(sqlPrincipal.getJob());
            relationship.setCharacters(sqlPrincipal.getCharacters());
            relationship.setPerson(person);

            if (movie.getPrincipals() == null) {
                movie.setPrincipals(new HashSet<>());
            }
            movie.getPrincipals().add(relationship);
        }

        neo4jMovieRepo.save(movie);
    }

    /**
     * Synchronise tous les utilisateurs
     */
    @Transactional
    public void syncAllUsers() {
        log.info("Synchronisation des utilisateurs...");
        List<User> users = sqlUserRepo.findAll();

        int count = 0;
        for (User user : users) {
            syncUser(user);
            count++;
        }

        log.info("{} utilisateurs synchronisés", count);
    }

    /**
     * Synchronise un utilisateur unique
     */
    @Transactional
    public UserNode syncUser(User sqlUser) {
        UserNode userNode = new UserNode();
        userNode.setUsername(sqlUser.getUsername());
        userNode.setEmail(sqlUser.getEmail());

        return neo4jUserRepo.save(userNode);
    }

    /**
     * Synchronise les relations Film-Lieu (appelée après syncAllMovies et
     * syncAllLocations)
     */
    @Transactional
    public void syncMovieLocationRelations() {
        log.info("Synchronisation des relations Film-Lieu...");
        syncAllLocations(); // Inclut déjà la création des relations
        log.info("Relations Film-Lieu synchronisées");
    }

    /**
     * Synchronise les relations Film-Réalisateur (appelée après syncAllMovies et
     * syncAllPersons)
     */
    @Transactional
    public void syncMovieDirectorRelations() {
        log.info("Synchronisation des relations Film-Réalisateur...");
        syncAllPrincipals(); // Inclut déjà la création des relations
        log.info("Relations Film-Réalisateur synchronisées");
    }
}
