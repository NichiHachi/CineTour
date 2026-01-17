package com.polytech.crud.neo4j.service;

import com.polytech.crud.neo4j.entity.MovieNode;
import com.polytech.crud.neo4j.entity.PersonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service pour des requêtes Cypher avancées
 * Exploite pleinement les capacités de graphe de Neo4j
 */
@Service
@RequiredArgsConstructor
public class AdvancedGraphQueryService {

    private final Neo4jClient neo4jClient;

    /**
     * Trouve les films ayant au moins N acteurs en commun
     */
    public List<Map<String, Object>> findMoviesWithCommonActors(String idImdb, int minCommonActors) {
        String cypher = """
                MATCH (m1:Movie {idImdb: $idImdb})-[:HAS_PRINCIPAL]->(p:Person)<-[:HAS_PRINCIPAL]-(m2:Movie)
                WHERE m1 <> m2
                WITH m2, COUNT(DISTINCT p) as commonActors
                WHERE commonActors >= $minCommonActors
                RETURN m2.title as title, m2.idImdb as idImdb, m2.releaseYear as year, commonActors
                ORDER BY commonActors DESC
                """;

        return neo4jClient.query(cypher)
                .bind(idImdb).to("idImdb")
                .bind(minCommonActors).to("minCommonActors")
                .fetch()
                .all().stream().toList();
    }

    /**
     * Trouve les "degrés de séparation" entre deux acteurs (Bacon number style)
     */
    public List<Map<String, Object>> findShortestPathBetweenActors(String nconst1, String nconst2) {
        String cypher = """
                MATCH path = shortestPath(
                    (p1:Person {nconst: $nconst1})-[:HAS_PRINCIPAL*]-(p2:Person {nconst: $nconst2})
                )
                RETURN [node IN nodes(path) |
                    CASE
                        WHEN 'Person' IN labels(node) THEN node.primaryName
                        WHEN 'Movie' IN labels(node) THEN node.title
                    END
                ] as path,
                length(path) as degrees
                """;

        return neo4jClient.query(cypher)
                .bind(nconst1).to("nconst1")
                .bind(nconst2).to("nconst2")
                .fetch()
                .all().stream().toList();
    }

    /**
     * Trouve les collaborations fréquentes (acteurs qui jouent souvent ensemble)
     */
    public List<Map<String, Object>> findFrequentCollaborations(int minCollaborations) {
        String cypher = """
                MATCH (p1:Person)<-[:HAS_PRINCIPAL]-(m:Movie)-[:HAS_PRINCIPAL]->(p2:Person)
                WHERE id(p1) < id(p2)
                WITH p1, p2, COLLECT(m.title) as movies, COUNT(m) as collaborations
                WHERE collaborations >= $minCollaborations
                RETURN p1.primaryName as actor1,
                       p2.primaryName as actor2,
                       collaborations,
                       movies
                ORDER BY collaborations DESC
                """;

        return neo4jClient.query(cypher)
                .bind(minCollaborations).to("minCollaborations")
                .fetch()
                .all().stream().toList();
    }

    /**
     * Recommandations de films basées sur les lieux de tournage similaires
     */
    public List<Map<String, Object>> recommendMoviesByLocation(String idImdb, String countryCode) {
        String cypher = """
                MATCH (m:Movie {idImdb: $idImdb})-[:FILMED_AT]->(l1:Location {countryCode: $countryCode})
                WITH COLLECT(DISTINCT l1.displayName) as locations
                MATCH (other:Movie)-[:FILMED_AT]->(l2:Location {countryCode: $countryCode})
                WHERE other.idImdb <> $idImdb AND l2.displayName IN locations
                WITH other, COUNT(DISTINCT l2) as commonLocations
                RETURN other.title as title,
                       other.idImdb as idImdb,
                       other.releaseYear as year,
                       commonLocations
                ORDER BY commonLocations DESC
                LIMIT 10
                """;

        return neo4jClient.query(cypher)
                .bind(idImdb).to("idImdb")
                .bind(countryCode).to("countryCode")
                .fetch()
                .all().stream().toList();
    }

    /**
     * Analyse la carrière d'un réalisateur (évolution dans le temps)
     */
    public List<Map<String, Object>> analyzeDirectorCareer(String nconst) {
        String cypher = """
                MATCH (p:Person {nconst: $nconst})<-[:DIRECTED_BY]-(m:Movie)
                OPTIONAL MATCH (m)-[:HAS_RATING]->(r:Rating)
                RETURN m.releaseYear as year,
                       m.title as title,
                       r.averageRating as rating,
                       r.numVotes as votes
                ORDER BY year ASC
                """;

        return neo4jClient.query(cypher)
                .bind(nconst).to("nconst")
                .fetch()
                .all().stream().toList();
    }

    /**
     * Trouve les lieux de tournage "populaires" (utilisés dans plusieurs films)
     */
    public List<Map<String, Object>> findPopularFilmingLocations(int minMovies) {
        String cypher = """
                MATCH (l:Location)<-[:FILMED_AT]-(m:Movie)
                WITH l, COLLECT(m.title) as movies, COUNT(m) as movieCount
                WHERE movieCount >= $minMovies
                RETURN l.displayName as location,
                       l.countryCode as country,
                       l.latitude as latitude,
                       l.longitude as longitude,
                       movieCount,
                       movies
                ORDER BY movieCount DESC
                """;

        return neo4jClient.query(cypher)
                .bind(minMovies).to("minMovies")
                .fetch()
                .all().stream().toList();
    }

    /**
     * Analyse les tendances de genres par décennie
     */
    public List<Map<String, Object>> analyzeGenreTrends() {
        String cypher = """
                MATCH (m:Movie)
                WHERE m.releaseYear IS NOT NULL AND m.genres IS NOT NULL
                WITH (m.releaseYear / 10) * 10 as decade, m.genres as genres
                UNWIND SPLIT(genres, ',') as genre
                WITH decade, TRIM(genre) as genre, COUNT(*) as count
                RETURN decade, genre, count
                ORDER BY decade DESC, count DESC
                """;

        return neo4jClient.query(cypher)
                .fetch()
                .all().stream().toList();
    }

    /**
     * Trouve les "super-connecteurs" (personnes ayant travaillé avec le plus de
     * monde)
     */
    public List<Map<String, Object>> findMostConnectedPeople(int limit) {
        String cypher = """
                MATCH (p1:Person)-[:HAS_PRINCIPAL]-(m:Movie)-[:HAS_PRINCIPAL]-(p2:Person)
                WHERE p1 <> p2
                WITH p1, COUNT(DISTINCT p2) as connections, COUNT(DISTINCT m) as movies
                RETURN p1.primaryName as name,
                       p1.nconst as nconst,
                       connections,
                       movies
                ORDER BY connections DESC
                LIMIT $limit
                """;

        return neo4jClient.query(cypher)
                .bind(limit).to("limit")
                .fetch()
                .all().stream().toList();
    }
}
