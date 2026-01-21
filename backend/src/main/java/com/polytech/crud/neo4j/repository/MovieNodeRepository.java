package com.polytech.crud.neo4j.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.polytech.crud.neo4j.entity.MovieNode;

@Repository
public interface MovieNodeRepository extends Neo4jRepository<MovieNode, Long> {

    Optional<MovieNode> findByIdImdb(String idImdb);

    List<MovieNode> findByTitleContainingIgnoreCase(String title);

    @Query("MATCH (m:Movie) WHERE m.release_year = $year RETURN m")
    List<MovieNode> findByReleaseYear(@Param("year") Integer year);

    @Query("MATCH (m:Movie)-[:FILMED_AT]->(l:Location) WHERE m.id_imdb = $idImdb RETURN m, l")
    Optional<MovieNode> findByIdImdbWithLocations(@Param("idImdb") String idImdb);

    @Query("MATCH (m:Movie)-[:DIRECTED]-(p:Person) WHERE m.id_imdb = $idImdb RETURN m, p")
    Optional<MovieNode> findByIdImdbWithDirectors(@Param("idImdb") String idImdb);

    /**
     * Trouve un film avec seulement directeurs et genres (pas de relations
     * optionnelles)
     * Pour éviter les erreurs de mapping avec HAS_PRINCIPAL/KNOWN_FOR manquants
     */
    @Query("""
                MATCH (m:Movie {id_imdb: $idImdb})
                OPTIONAL MATCH (p:Person)-[:DIRECTED]->(m)
                RETURN m, collect(p) AS directors
            """)
    Optional<MovieNode> findByIdImdbForRecommendations(@Param("idImdb") String idImdb);

    @Query("MATCH (m:Movie)-[r:HAS_PRINCIPAL]->(pr:Person) WHERE m.id_imdb = $idImdb RETURN m, r, pr")
    Optional<MovieNode> findByIdImdbWithPrincipals(@Param("idImdb") String idImdb);

    // ========== Recommandations de films similaires ==========

    /**
     * Trouve des films avec les mêmes directeurs
     */
    @Query("""
                MATCH (p:Person)-[:DIRECTED]->(m1:Movie {id_imdb: $idImdb})
                MATCH (p)-[:DIRECTED]->(m2:Movie)
                WHERE m1.id_imdb <> m2.id_imdb
                WITH DISTINCT m2
                RETURN m2
                LIMIT $limit
            """)
    List<MovieNode> findMoviesBySameDirectors(@Param("idImdb") String idImdb, @Param("limit") int limit);

    /**
     * Trouve des films avec les mêmes acteurs principaux
     * Note: Cette requête sera fonctionnelle quand les relations HAS_PRINCIPAL
     * seront disponibles
     */
    @Query("""
                MATCH (m1:Movie {id_imdb: $idImdb})
                MATCH (m2:Movie)
                WHERE m1.id_imdb <> m2.id_imdb
                    AND m1.genres IS NOT NULL
                    AND m2.genres IS NOT NULL
                    AND any(g IN split(m1.genres, ',') WHERE m2.genres CONTAINS g)
                RETURN DISTINCT m2
                LIMIT $limit
            """)
    List<MovieNode> findMoviesBySameActors(@Param("idImdb") String idImdb, @Param("limit") int limit);

    /**
     * Trouve des films du même genre
     */
    @Query("""
                MATCH (m1:Movie {id_imdb: $idImdb})
                MATCH (m2:Movie)
                WHERE m1.id_imdb <> m2.id_imdb
                    AND m1.genres IS NOT NULL
                    AND m2.genres IS NOT NULL
                    AND any(g IN split(m1.genres, ',') WHERE m2.genres CONTAINS g)
                RETURN DISTINCT m2
                LIMIT $limit
            """)
    List<MovieNode> findMoviesBySameGenre(@Param("idImdb") String idImdb,
            @Param("minRating") double minRating,
            @Param("limit") int limit);

    /**
     * Recommandations combinées : directeurs et genre
     * Retourne les films avec leurs directeurs chargés
     */
    @Query("""
                MATCH (m1:Movie {id_imdb: $idImdb})

                // Films avec mêmes directeurs
                OPTIONAL MATCH (p:Person)-[:DIRECTED]->(m1)
                OPTIONAL MATCH (p)-[:DIRECTED]->(m2:Movie)
                WHERE m2.id_imdb <> m1.id_imdb

                // Tous les films du même genre
                OPTIONAL MATCH (m3:Movie)
                WHERE m3.id_imdb <> m1.id_imdb
                    AND m1.genres IS NOT NULL
                    AND m3.genres IS NOT NULL
                    AND any(g IN split(m1.genres, ',') WHERE m3.genres CONTAINS g)

                // Combiner tous les films candidats avec leurs directeurs
                WITH collect(DISTINCT {movie: m2, fromDirector: true}) +
                     collect(DISTINCT {movie: m3, fromDirector: false}) as allCandidates
                UNWIND allCandidates as candidate
                WITH DISTINCT candidate.movie as movie
                WHERE movie IS NOT NULL

                // Charger les directeurs pour chaque film candidat
                OPTIONAL MATCH (director:Person)-[:DIRECTED]->(movie)

                WITH movie, collect(DISTINCT director) as directors

                RETURN movie, directors
                LIMIT $limit
            """)
    List<MovieNode> findRecommendedMoviesComplex(@Param("idImdb") String idImdb,
            @Param("minRating") double minRating,
            @Param("limit") int limit);

    /**
     * Charge un film avec ses directeurs
     */
    @Query("""
                MATCH (m:Movie {id_imdb: $idImdb})
                OPTIONAL MATCH (p:Person)-[:DIRECTED]->(m)
                RETURN m, collect(p) as directors
            """)
    Optional<MovieNode> findByIdImdbWithDirectorsLoaded(@Param("idImdb") String idImdb);

    /**
     * Trouve des films similaires par période temporelle et genre
     */
    @Query("""
                MATCH (m1:Movie {id_imdb: $idImdb})
                MATCH (m2:Movie)
                WHERE m1.id_imdb <> m2.id_imdb
                    AND m2.release_year IS NOT NULL
                    AND m1.release_year IS NOT NULL
                    AND toInteger(m2.release_year) IS NOT NULL
                    AND toInteger(m1.release_year) IS NOT NULL
                    AND abs(toInteger(m2.release_year) - toInteger(m1.release_year)) <= $yearRange
                    AND m1.genres IS NOT NULL
                    AND m2.genres IS NOT NULL
                    AND any(g IN split(m1.genres, ',') WHERE m2.genres CONTAINS g)
                RETURN DISTINCT m2
                LIMIT $limit
            """)
    List<MovieNode> findMoviesBySameEraAndGenre(@Param("idImdb") String idImdb,
            @Param("yearRange") int yearRange,
            @Param("minRating") double minRating,
            @Param("limit") int limit);

    /**
     * Récupère les nconst des directors d'un film
     */
    @Query("""
                MATCH (p:Person)-[:DIRECTED]->(m:Movie {id_imdb: $idImdb})
                RETURN p.nconst as nconst
            """)
    List<String> findDirectorNconstsByMovieIdImdb(@Param("idImdb") String idImdb);
}
