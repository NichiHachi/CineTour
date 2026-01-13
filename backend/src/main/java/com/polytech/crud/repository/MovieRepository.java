package com.polytech.crud.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.polytech.crud.entity.Location;
import com.polytech.crud.entity.Movie;
import com.polytech.crud.entity.User;
import com.polytech.crud.projection.MovieGenreCount;

public interface MovieRepository extends Neo4jRepository<Movie, Long> {
    List<Movie> findByTitle(String name);

    Movie findByIdImdb(String idImdb);

    @Query("MATCH (m:Movie) WHERE toLower(m.title) CONTAINS toLower($query) RETURN m ORDER BY (m.movieSearchCount + m.locationSearchCount) DESC LIMIT 10")
    List<Movie> searchByTitleContainingOrderBySearchCount(@Param("query") String query);

    // Requêtes de test pour afficher les données
    @Query("MATCH (m:Movie) RETURN count(m)")
    Long countAllMovies();

    @Query("MATCH (m:Movie) WHERE m.releaseYear = $year RETURN m ORDER BY m.title LIMIT 20")
    List<Movie> findByYear(@Param("year") Integer year);

    @Query("MATCH (m:Movie) RETURN m ORDER BY m.releaseYear DESC LIMIT $limit")
    List<Movie> findRecentMovies(@Param("limit") Integer limit);

    @Query("MATCH (m:Movie) RETURN m ORDER BY (m.movieSearchCount + m.locationSearchCount) DESC LIMIT $limit")
    List<Movie> findMostPopularMovies(@Param("limit") Integer limit);

    @Query("MATCH (m:Movie) WHERE m.genres CONTAINS $genre RETURN m LIMIT 20")
    List<Movie> findByGenre(@Param("genre") String genre);

    // Requêtes pour explorer les voisins dans le graphe
    @Query("MATCH (m:Movie {idImdb: $idImdb})-[r]-(n) RETURN type(r) as relationshipType, labels(n) as nodeLabels, count(n) as count")
    List<Map<String, Object>> findNeighborsSummary(@Param("idImdb") String idImdb);

    @Query("MATCH (m:Movie {idImdb: $idImdb})-[:FILMED_AT]->(l:Location) RETURN l")
    List<Location> findLocationsByMovie(@Param("idImdb") String idImdb);

    @Query("MATCH (l:Location {locationString: $location})<-[:FILMED_AT]-(m:Movie) RETURN m LIMIT 20")
    List<Movie> findMoviesByLocation(@Param("location") String location);

    @Query("MATCH (u:User {username: $username})-[:HAS_SEARCH_HISTORY]->(h:MovieSearchHistory)-[:SEARCHED_MOVIE]->(m:Movie) RETURN DISTINCT m ORDER BY h.searchedAt DESC LIMIT 10")
    List<Movie> findMoviesSearchedByUser(@Param("username") String username);

    @Query("MATCH (m:Movie {idImdb: $idImdb})<-[:SEARCHED_MOVIE]-(h:MovieSearchHistory)<-[:HAS_SEARCH_HISTORY]-(u:User) RETURN DISTINCT u")
    List<User> findUsersWhoSearchedMovie(@Param("idImdb") String idImdb);

    @Query("MATCH (m1:Movie {idImdb: $idImdb})-[:FILMED_AT]->(l:Location)<-[:FILMED_AT]-(m2:Movie) WHERE m1 <> m2 RETURN DISTINCT m2, count(l) as sharedLocations ORDER BY sharedLocations DESC LIMIT 10")
    List<Movie> findMoviesWithSharedLocations(@Param("idImdb") String idImdb);

    @Query("MATCH path = (m:Movie {idImdb: $idImdb})-[*1..2]-(n) RETURN DISTINCT labels(n) as nodeType, count(n) as count ORDER BY count DESC")
    List<Map<String, Object>> findNeighborsAtDistance(@Param("idImdb") String idImdb);

    // Nouvelles requêtes pour le modèle étendu
    @Query("MATCH (m:Movie)-[r:SHARES_ACTOR_WITH]->(m2:Movie) WHERE m.title = $title RETURN m.title AS Movie1, m2.title AS Movie2, r.sharedActors AS SharedActors LIMIT 25")
    List<Map<String, Object>> findMoviesWithSharedActorsByTitle(@Param("title") String title);

    @Query("MATCH (m:Movie {title: $title})-[:HAS_GENRE]->(g:Genre) RETURN g.name")
    List<String> findGenresByMovieTitle(@Param("title") String title);

    @Query("MATCH (p:Person)-[:ACTED_IN]->(m:Movie {title: $title}) RETURN p.name")
    List<String> findActorsByMovieTitle(@Param("title") String title);

    @Query("MATCH (p:Person)-[:DIRECTED]->(m:Movie {title: $title}) RETURN p.name")
    List<String> findDirectorsByMovieTitle(@Param("title") String title);

    @Query("MATCH (m:Movie {title: $title})-[:PRODUCED_BY]->(p:Producer) RETURN p.name")
    List<String> findProducersByMovieTitle(@Param("title") String title);

    @Query("MATCH (m:Movie {title: $title})-[:NARRATIVE_LOCATION|FILM_LOCATION]->(l:Location) RETURN l.name AS name, l.longitude AS longitude, l.latitude AS latitude")
    List<Map<String, Object>> findLocationsByMovieTitle(@Param("title") String title);

    @Query("MATCH (m:Movie)-[:HAS_GENRE]->(g:Genre) RETURN g.name AS Genre, avg(m.reviewScore) AS AverageRating ORDER BY AverageRating DESC")
    List<Map<String, Object>> findAverageRatingByGenre();

    // Requêtes pour explorer les films par genre partagé
    @Query("MATCH (m1:Movie {title: $title})-[:HAS_GENRE]->(g:Genre)<-[:HAS_GENRE]-(m2:Movie) WHERE m1 <> m2 WITH m2.title AS title, count(DISTINCT g) AS genreCount RETURN title, genreCount ORDER BY genreCount DESC LIMIT 20")
    List<MovieGenreCount> findMoviesWithSharedGenres(@Param("title") String title);

    @Query("MATCH (m:Movie {title: $title})-[:HAS_GENRE]->(g:Genre {name: $genre})<-[:HAS_GENRE]-(m2:Movie) WHERE m <> m2 RETURN DISTINCT m2.title ORDER BY m2.reviewScore DESC LIMIT 10")
    List<String> findMoviesBySameGenre(@Param("title") String title, @Param("genre") String genre);

    @Query("MATCH (m1:Movie {title: $title})-[:HAS_GENRE]->(g:Genre)<-[:HAS_GENRE]-(m2:Movie) WHERE m1 <> m2 RETURN g.name AS genre, count(DISTINCT m2) AS movieCount ORDER BY movieCount DESC")
    List<Map<String, Object>> findSharedGenresSummary(@Param("title") String title);
}
