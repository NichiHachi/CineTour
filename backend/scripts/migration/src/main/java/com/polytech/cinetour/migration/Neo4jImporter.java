package com.polytech.cinetour.migration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import static org.neo4j.driver.Values.parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Importe les données dans Neo4j depuis un dump SQL parsé
 */
public class Neo4jImporter implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Neo4jImporter.class);

    private final Driver driver;

    public Neo4jImporter(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));

        // Test de connexion
        try (Session session = driver.session()) {
            session.run("RETURN 1").consume();
            logger.info("✓ Connecté à Neo4j");
        } catch (Exception e) {
            logger.error("Erreur connexion Neo4j: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        driver.close();
        logger.info("✓ Connexion Neo4j fermée");
    }

    public void clearDatabase() {
        try (Session session = driver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
            logger.info("Base Neo4j nettoyée");
        }
    }

    public void createIndexes() {
        try (Session session = driver.session()) {
            String[] indexes = {
                    "CREATE INDEX movie_imdb IF NOT EXISTS FOR (m:Movie) ON (m.idImdb)",
                    "CREATE INDEX person_nconst IF NOT EXISTS FOR (p:Person) ON (p.nconst)",
                    "CREATE INDEX director_imdb IF NOT EXISTS FOR (d:Director) ON (d.idImdb)",
                    "CREATE INDEX location_imdb IF NOT EXISTS FOR (l:Location) ON (l.idImdb)",
                    "CREATE INDEX user_email IF NOT EXISTS FOR (u:User) ON (u.email)",
                    "CREATE INDEX user_id IF NOT EXISTS FOR (u:User) ON (u.userId)",
                    "CREATE INDEX genre_name IF NOT EXISTS FOR (g:Genre) ON (g.name)"
            };

            for (String index : indexes) {
                session.run(index);
            }
            logger.info("Index créés");
        }
    }

    public void importData(Map<String, List<List<Object>>> data) {
        logger.info("\n Import des nœuds...");
        List<List<Object>> ratings = data.get("ratings");
        if (ratings == null) {
            ratings = new ArrayList<>();
        }

        importMovies(data.get("movies"), ratings);

        List<List<Object>> persons = data.get("persons");
        List<List<Object>> professions = data.get("person_primary_professions");
        List<List<Object>> knownTitles = data.get("person_known_for_titles");
        if (professions == null)
            professions = new ArrayList<>();
        if (knownTitles == null)
            knownTitles = new ArrayList<>();

        importPersons(persons, professions, knownTitles);
        importDirectors(data.get("directors"));
        importLocations(data.get("locations"));
        importUsers(data.get("users"));
        importGenres(data.get("movies"));

        logger.info("\n Création des relations...");
        importDirectorRelationships(data.get("director_directors"));
        importPrincipalRelationships(data.get("principals"));
        importLocationRelationships(data.get("locations"));
        importSearchHistory(data.get("movie_search_history"), data.get("users"));
        importGenreRelationships(data.get("movies"));
    }

    private void importMovies(List<List<Object>> movies, List<List<Object>> ratings) {
        // Créer un index des ratings par id_imdb
        Map<String, Map<String, Object>> ratingsMap = new HashMap<>();
        for (List<Object> rating : ratings) {
            // rating: (id, average_rating, id_imdb, num_votes)
            if (rating.size() >= 4 && rating.get(2) != null) {
                Map<String, Object> ratingData = new HashMap<>();
                ratingData.put("average_rating", rating.get(1));
                ratingData.put("num_votes", rating.get(3));
                ratingsMap.put(rating.get(2).toString(), ratingData);
            }
        }

        try (Session session = driver.session()) {
            int count = 0;
            for (List<Object> movie : movies) {
                // movie: (id, genres, id_imdb, image, location_search_count, locations_checked,
                // movie_search_count, release_year, runtime_minutes, title)
                if (movie.size() >= 10 && movie.get(2) != null) {
                    String idImdb = movie.get(2).toString();
                    Map<String, Object> ratingData = ratingsMap.getOrDefault(idImdb, new HashMap<>());

                    session.run("""
                            MERGE (m:Movie {idImdb: $idImdb})
                            SET m.title = $title,
                                m.releaseYear = $releaseYear,
                                m.runtimeMinutes = $runtimeMinutes,
                                m.genres = $genres,
                                m.image = $image,
                                m.movieSearchCount = $movieSearchCount,
                                m.locationSearchCount = $locationSearchCount,
                                m.locationsChecked = $locationsChecked,
                                m.averageRating = $averageRating,
                                m.numVotes = $numVotes
                            """,
                            parameters(
                                    "idImdb", idImdb,
                                    "title", movie.get(9),
                                    "releaseYear", movie.get(7),
                                    "runtimeMinutes", movie.get(8),
                                    "genres", movie.get(1),
                                    "image", movie.get(3),
                                    "movieSearchCount", movie.get(6),
                                    "locationSearchCount", movie.get(4),
                                    "locationsChecked", convertToBoolean(movie.get(5)),
                                    "averageRating", ratingData.get("average_rating"),
                                    "numVotes", ratingData.get("num_votes")));

                    count++;
                    if (count % 1000 == 0) {
                        logger.info("  → {} films importés...", count);
                    }
                }
            }
            logger.info("{} films importés", count);
        }
    }

    private void importPersons(List<List<Object>> persons, List<List<Object>> professions,
            List<List<Object>> knownTitles) {
        // Indexer les professions par person_id
        Map<Long, List<String>> profMap = new HashMap<>();
        for (List<Object> prof : professions) {
            if (prof.size() >= 2) {
                Long personId = ((Number) prof.get(0)).longValue();
                profMap.computeIfAbsent(personId, k -> new ArrayList<>());
                if (prof.get(1) != null) {
                    profMap.get(personId).add(prof.get(1).toString());
                }
            }
        }

        // Indexer les titres connus par person_id
        Map<Long, List<String>> titlesMap = new HashMap<>();
        for (List<Object> title : knownTitles) {
            if (title.size() >= 2) {
                Long personId = ((Number) title.get(0)).longValue();
                titlesMap.computeIfAbsent(personId, k -> new ArrayList<>());
                if (title.get(1) != null) {
                    titlesMap.get(personId).add(title.get(1).toString());
                }
            }
        }

        try (Session session = driver.session()) {
            int count = 0;
            for (List<Object> person : persons) {
                // person: (id, birth_year, death_year, nconst, primary_name)
                if (person.size() >= 5 && person.get(3) != null) {
                    Long personId = ((Number) person.get(0)).longValue();

                    session.run("""
                            MERGE (p:Person {nconst: $nconst})
                            SET p.primaryName = $primaryName,
                                p.birthYear = $birthYear,
                                p.deathYear = $deathYear,
                                p.primaryProfessions = $professions,
                                p.knownForTitles = $knownTitles
                            """,
                            parameters(
                                    "nconst", person.get(3),
                                    "primaryName", person.get(4),
                                    "birthYear", person.get(1),
                                    "deathYear", person.get(2),
                                    "professions", profMap.getOrDefault(personId, Collections.emptyList()),
                                    "knownTitles", titlesMap.getOrDefault(personId, Collections.emptyList())));

                    count++;
                    if (count % 1000 == 0) {
                        logger.info("  → {} personnes importées...", count);
                    }
                }
            }
            logger.info(" {} personnes importées", count);
        }
    }

    private void importDirectors(List<List<Object>> directors) {
        try (Session session = driver.session()) {
            int count = 0;
            for (List<Object> director : directors) {
                // director: (id, id_imdb)
                if (director.size() >= 2 && director.get(1) != null) {
                    session.run(
                            "MERGE (d:Director {idImdb: $idImdb})",
                            parameters("idImdb", director.get(1)));
                    count++;
                    if (count % 1000 == 0) {
                        logger.info("  → {} réalisateurs importés...", count);
                    }
                }
            }
            logger.info("{} réalisateurs importés", count);
        }
    }

    private void importLocations(List<List<Object>> locations) {
        try (Session session = driver.session()) {
            int count = 0;
            for (List<Object> location : locations) {
                // location: (id, description, id_imdb, location_string, [latitude, longitude,
                // display_name, country_code])
                if (location.size() >= 4 && location.get(2) != null && location.get(3) != null) {
                    session.run("""
                            MERGE (l:Location {idImdb: $idImdb, locationString: $locationString})
                            SET l.description = $description,
                                l.latitude = $latitude,
                                l.longitude = $longitude,
                                l.displayName = $displayName,
                                l.countryCode = $countryCode
                            """,
                            parameters(
                                    "idImdb", location.get(2),
                                    "locationString", location.get(3),
                                    "description", location.get(1),
                                    "latitude", location.size() > 4 ? location.get(4) : null,
                                    "longitude", location.size() > 5 ? location.get(5) : null,
                                    "displayName", location.size() > 6 ? location.get(6) : null,
                                    "countryCode", location.size() > 7 ? location.get(7) : null));
                    count++;
                }
            }
            logger.info("{} lieux importés", count);
        }
    }

    private void importUsers(List<List<Object>> users) {
        try (Session session = driver.session()) {
            int count = 0;
            for (List<Object> user : users) {
                // user: (id, email, password, username)
                if (user.size() >= 4 && user.get(1) != null) {
                    session.run("""
                            MERGE (u:User {email: $email})
                            SET u.userId = $userId,
                                u.username = $username,
                                u.password = $password
                            """,
                            parameters(
                                    "userId", user.get(0),
                                    "email", user.get(1),
                                    "username", user.get(3),
                                    "password", user.get(2)));
                    count++;
                }
            }
            logger.info("{} utilisateurs importés", count);
        }
    }

    private void importGenres(List<List<Object>> movies) {
        // Extraire tous les genres uniques
        Set<String> allGenres = new HashSet<>();
        for (List<Object> movie : movies) {
            // movie: (id, genres, id_imdb, ...)
            if (movie.size() >= 2 && movie.get(1) != null) {
                String genresString = movie.get(1).toString();
                String[] genres = genresString.split(",");
                for (String genre : genres) {
                    allGenres.add(genre.trim());
                }
            }
        }

        // Créer les nœuds Genre
        try (Session session = driver.session()) {
            for (String genre : allGenres) {
                session.run(
                        "MERGE (g:Genre {name: $name})",
                        parameters("name", genre));
            }
            logger.info("{} genres importés", allGenres.size());
        }
    }

    private void importDirectorRelationships(List<List<Object>> directorDirectors) {
        try (Session session = driver.session()) {
            int count = 0;
            for (List<Object> row : directorDirectors) {
                // (director_id, directors/movie_imdb)
                if (row.size() >= 2 && row.get(1) != null) {
                    Result result = session.run("""
                            MATCH (d:Director)
                            MATCH (m:Movie {idImdb: $movieImdb})
                            MERGE (d)-[:DIRECTED]->(m)
                            RETURN count(*) as cnt
                            """,
                            parameters("movieImdb", row.get(1)));

                    if (result.hasNext() && result.single().get("cnt").asLong() > 0) {
                        count++;
                        if (count % 1000 == 0) {
                            logger.info(" {} relations DIRECTED créées...", count);
                        }
                    }
                }
            }
            logger.info(" {} relations DIRECTED créées", count);
        }
    }

    private void importPrincipalRelationships(List<List<Object>> principals) {
        try (Session session = driver.session()) {
            int count = 0;
            for (List<Object> principal : principals) {
                // principal: (id, category, characters, id_imdb, job, nconst, ordering)
                if (principal.size() >= 7 && principal.get(5) != null && principal.get(3) != null) {
                    String category = principal.get(1) != null ? principal.get(1).toString() : null;
                    String relationType = getRelationType(category);

                    if (relationType != null) {
                        session.run(
                                String.format("""
                                        MATCH (p:Person {nconst: $nconst})
                                        MATCH (m:Movie {idImdb: $idImdb})
                                        MERGE (p)-[r:%s]->(m)
                                        SET r.ordering = $ordering,
                                            r.characters = $characters,
                                            r.job = $job
                                        """, relationType),
                                parameters(
                                        "nconst", principal.get(5),
                                        "idImdb", principal.get(3),
                                        "ordering", principal.get(6),
                                        "characters", principal.get(2),
                                        "job", principal.get(4)));
                        count++;
                        if (count % 1000 == 0) {
                            logger.info("  → {} relations principals créées...", count);
                        }
                    }
                }
            }
            logger.info(" {} relations principals créées", count);
        }
    }

    private void importLocationRelationships(List<List<Object>> locations) {
        try (Session session = driver.session()) {
            int count = 0;
            for (List<Object> location : locations) {
                if (location.size() >= 4 && location.get(2) != null && location.get(3) != null) {
                    session.run("""
                            MATCH (m:Movie {idImdb: $idImdb})
                            MATCH (l:Location {idImdb: $idImdb, locationString: $locationString})
                            MERGE (m)-[:FILMED_AT]->(l)
                            """,
                            parameters(
                                    "idImdb", location.get(2),
                                    "locationString", location.get(3)));
                    count++;
                }
            }
            logger.info(" {} relations FILMED_AT créées", count);
        }
    }

    private void importSearchHistory(List<List<Object>> searchHistory, List<List<Object>> users) {
        // Créer un mapping user_id -> email
        Map<Long, String> userEmails = new HashMap<>();
        for (List<Object> user : users) {
            if (user.size() >= 2) {
                userEmails.put(((Number) user.get(0)).longValue(), user.get(1).toString());
            }
        }

        try (Session session = driver.session()) {
            int count = 0;
            for (List<Object> search : searchHistory) {
                // search: (id, id_imdb, movie_title, search_time, user_id)
                if (search.size() >= 5 && search.get(1) != null && search.get(4) != null) {
                    Long userId = ((Number) search.get(4)).longValue();
                    String userEmail = userEmails.get(userId);

                    if (userEmail != null) {
                        // Convertir le datetime MySQL au format ISO pour Neo4j
                        String searchTime = null;
                        if (search.get(3) != null) {
                            String mysqlDateTime = search.get(3).toString();
                            // Format MySQL: 2026-01-15 10:30:00.000000
                            // Format Neo4j ISO: 2026-01-15T10:30:00.000Z
                            searchTime = mysqlDateTime.replace(" ", "T").replaceAll("\\.\\d+$", "") + "Z";
                        }

                        session.run("""
                                MATCH (u:User {email: $email})
                                MATCH (m:Movie {idImdb: $idImdb})
                                MERGE (u)-[s:SEARCHED]->(m)
                                SET s.searchTime = datetime($searchTime)
                                """,
                                parameters(
                                        "email", userEmail,
                                        "idImdb", search.get(1),
                                        "searchTime", searchTime));
                        count++;
                    }
                }
            }
            logger.info(" {} relations SEARCHED créées", count);
        }
    }

    private void importGenreRelationships(List<List<Object>> movies) {
        try (Session session = driver.session()) {
            int count = 0;
            for (List<Object> movie : movies) {
                // movie: (id, genres, id_imdb, ...)
                if (movie.size() >= 3 && movie.get(1) != null && movie.get(2) != null) {
                    String idImdb = movie.get(2).toString();
                    String genresString = movie.get(1).toString();
                    String[] genres = genresString.split(",");

                    for (String genre : genres) {
                        String genreTrimmed = genre.trim();
                        session.run("""
                                MATCH (m:Movie {idImdb: $idImdb})
                                MATCH (g:Genre {name: $genre})
                                MERGE (m)-[:HAS_GENRE]->(g)
                                """,
                                parameters(
                                        "idImdb", idImdb,
                                        "genre", genreTrimmed));
                        count++;
                    }
                }
            }
            logger.info(" {} relations HAS_GENRE créées", count);
        }
    }

    private String getRelationType(String category) {
        if (category == null)
            return null;

        return switch (category.toLowerCase()) {
            case "actor", "actress" -> "ACTED_IN";
            case "director" -> "DIRECTED";
            case "writer" -> "WROTE";
            case "producer" -> "PRODUCED";
            case "cinematographer" -> "CINEMATOGRAPHY";
            case "composer" -> "COMPOSED";
            case "editor" -> "EDITED";
            default -> null;
        };
    }

    private boolean convertToBoolean(Object value) {
        if (value == null)
            return false;
        if (value instanceof Boolean)
            return (Boolean) value;
        if (value instanceof Number)
            return ((Number) value).intValue() != 0;
        if (value instanceof String) {
            String str = value.toString().toLowerCase();
            return str.equals("true") || str.equals("1") || str.equals("yes");
        }
        return false;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(
                    "Usage: java Neo4jImporter <dump-file> <neo4j-password> [--clear] [--uri <uri>] [--user <user>]");
            System.exit(1);
        }

        String dumpFile = args[0];
        String password = args[1];
        String uri = "neo4j://localhost:7687";
        String user = "neo4j";
        boolean clear = false;

        // Parser les arguments
        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "--clear" -> clear = true;
                case "--uri" -> {
                    if (i + 1 < args.length)
                        uri = args[++i];
                }
                case "--user" -> {
                    if (i + 1 < args.length)
                        user = args[++i];
                }
            }
        }

        logger.info("=== Import MySQL dump → Neo4j ===\n");

        try {
            // Parser le dump
            SQLDumpParser parser = new SQLDumpParser(dumpFile);
            Map<String, List<List<Object>>> data = parser.parse();

            // Importer dans Neo4j
            try (Neo4jImporter importer = new Neo4jImporter(uri, user, password)) {
                if (clear) {
                    logger.info("\n  Nettoyage de Neo4j...");
                    importer.clearDatabase();
                }

                logger.info("\n  Création des index...");
                importer.createIndexes();

                importer.importData(data);

                logger.info("\n  Import terminé avec succès!");
            }
        } catch (Exception e) {
            logger.error(" Erreur lors de l'import: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
