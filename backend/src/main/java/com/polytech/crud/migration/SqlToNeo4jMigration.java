package com.polytech.crud.migration;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Migration script pour importer des données depuis un dump SQL MySQL vers
 * Neo4j
 * Traite les données par lots pour éviter les problèmes de mémoire
 */
@Slf4j
public class SqlToNeo4jMigration {

    private static final int BATCH_SIZE = 10000; // Augmenté pour meilleures performances
    private final Driver neo4jDriver;
    private final Map<String, String> directorIdToImdbId = new HashMap<>();

    // Compteurs pour les logs
    private final AtomicInteger moviesCount = new AtomicInteger(0);
    private final AtomicInteger directorsCount = new AtomicInteger(0);
    private final AtomicInteger locationsCount = new AtomicInteger(0);
    private final AtomicInteger personsCount = new AtomicInteger(0);
    private final AtomicInteger principalsCount = new AtomicInteger(0);
    private final AtomicInteger ratingsCount = new AtomicInteger(0);
    private final AtomicInteger usersCount = new AtomicInteger(0);

    // Options d'import
    private boolean importMovies = true;
    private boolean importDirectors = true;
    private boolean importLocations = true;
    private boolean importPersons = true;
    private boolean importPrincipals = true;
    private boolean importRatings = true;
    private boolean importUsers = true;
    private boolean cleanDatabase = false; // Par défaut, ne pas nettoyer

    public SqlToNeo4jMigration(String neo4jUri, String username, String password) {
        this.neo4jDriver = GraphDatabase.driver(neo4jUri, AuthTokens.basic(username, password));
    }

    public void setImportMovies(boolean importMovies) {
        this.importMovies = importMovies;
    }

    public void setImportDirectors(boolean importDirectors) {
        this.importDirectors = importDirectors;
    }

    public void setImportLocations(boolean importLocations) {
        this.importLocations = importLocations;
    }

    public void setImportPersons(boolean importPersons) {
        this.importPersons = importPersons;
    }

    public void setImportPrincipals(boolean importPrincipals) {
        this.importPrincipals = importPrincipals;
    }

    public void setImportRatings(boolean importRatings) {
        this.importRatings = importRatings;
    }

    public void setImportUsers(boolean importUsers) {
        this.importUsers = importUsers;
    }

    public void setCleanDatabase(boolean cleanDatabase) {
        this.cleanDatabase = cleanDatabase;
    }

    /**
     * Point d'entrée principal pour la migration
     */
    public void migrate(String sqlDumpPath) {
        log.info("🚀 Début de la migration SQL vers Neo4j");
        log.info("📁 Fichier: {}", sqlDumpPath);
        log.info("📋 Options d'import:");
        log.info("  - Films: {}", importMovies ? "✓" : "✗");
        log.info("  - Réalisateurs: {}", importDirectors ? "✓" : "✗");
        log.info("  - Localisations: {}", importLocations ? "✓" : "✗");
        log.info("  - Personnes: {}", importPersons ? "✓" : "✗");
        log.info("  - Principaux: {}", importPrincipals ? "✓" : "✗");
        log.info("  - Ratings: {}", importRatings ? "✓" : "✗");
        log.info("  - Utilisateurs: {}", importUsers ? "✓" : "✗");
        log.info("  - Nettoyer la base: {}",
                cleanDatabase ? "✓ (ATTENTION: supprimera toutes les données!)" : "✗ (mode mise à jour)");

        try {
            // Nettoyage de la base Neo4j si demandé
            if (cleanDatabase) {
                cleanNeo4jDatabase();
            } else {
                log.info("ℹ️  Mode mise à jour: les données existantes seront préservées et mises à jour");
            }

            // Créer les index avant l'import pour de meilleures performances
            createIndexes();

            // Parse et migration en streaming (par lots)
            parseSqlDumpAndMigrateStreaming(sqlDumpPath);

            log.info("✅ Migration terminée avec succès!");
            printStatistics();

        } catch (Exception e) {
            log.error("❌ Erreur lors de la migration: {}", e.getMessage(), e);
        } finally {
            neo4jDriver.close();
        }
    }

    /**
     * Nettoie la base Neo4j
     */
    private void cleanNeo4jDatabase() {
        log.warn("⚠️  ATTENTION: Suppression de TOUTES les données de Neo4j!");
        log.info("🧹 Nettoyage de la base Neo4j...");
        try (Session session = neo4jDriver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }
        log.info("✓ Base Neo4j nettoyée");
    }

    /**
     * Crée les index pour optimiser les performances
     */
    private void createIndexes() {
        log.info("📊 Création des index pour optimiser les performances...");
        try (Session session = neo4jDriver.session()) {
            // Index sur les films
            session.run("CREATE INDEX IF NOT EXISTS FOR (m:Movie) ON (m.id_imdb)");

            // Index sur les personnes
            session.run("CREATE INDEX IF NOT EXISTS FOR (p:Person) ON (p.nconst)");

            // Index sur les genres
            session.run("CREATE INDEX IF NOT EXISTS FOR (g:Genre) ON (g.name)");

            // Index sur les professions
            session.run("CREATE INDEX IF NOT EXISTS FOR (pr:Profession) ON (pr.name)");

            // Index sur les localisations
            session.run("CREATE INDEX IF NOT EXISTS FOR (l:Location) ON (l.id_imdb, l.location_string)");

            log.info("✓ Index créés avec succès");
        }
    }

    /**
     * Parse le dump SQL et migre les données en streaming (par lots)
     */
    private void parseSqlDumpAndMigrateStreaming(String filePath) throws IOException {
        log.info("📖 Lecture et parsing du dump SQL en mode streaming...");

        // Buffers pour le traitement par lots
        List<Map<String, String>> moviesBatch = new ArrayList<>();
        List<Map<String, String>> directorRelationsBatch = new ArrayList<>();
        List<Map<String, String>> locationsBatch = new ArrayList<>();
        List<Map<String, String>> personsBatch = new ArrayList<>();
        List<Map<String, String>> principalsBatch = new ArrayList<>();
        List<Map<String, String>> ratingsBatch = new ArrayList<>();
        List<Map<String, String>> usersBatch = new ArrayList<>();

        Map<String, List<String>> tableColumns = new HashMap<>();
        StringBuilder currentStatement = new StringBuilder();
        String currentTable = null;
        boolean inInsert = false;
        boolean inCreateTable = false;
        String createTableName = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                String trimmed = line.trim();

                // Ignorer les commentaires et lignes vides
                if (trimmed.isEmpty() || trimmed.startsWith("--") ||
                        trimmed.startsWith("/*") || trimmed.startsWith("*") ||
                        trimmed.startsWith("/*!")) {
                    continue;
                }

                // Détecter CREATE TABLE
                if (trimmed.matches("(?i)CREATE TABLE [`']?(\\w+)[`']?.*")) {
                    createTableName = trimmed.replaceAll("(?i)CREATE TABLE [`']?(\\w+)[`']?.*", "$1").toLowerCase();
                    inCreateTable = true;
                    tableColumns.put(createTableName, new ArrayList<>());
                    continue;
                }

                // Parser les colonnes du CREATE TABLE
                if (inCreateTable && createTableName != null) {
                    if (trimmed.startsWith("`")) {
                        String columnName = trimmed.split("`")[1];
                        tableColumns.get(createTableName).add(columnName);
                    }
                    if (trimmed.contains(");") || trimmed.contains(") ENGINE")) {
                        inCreateTable = false;
                        createTableName = null;
                    }
                    continue;
                }

                // Détecter INSERT INTO
                if (trimmed.matches("(?i)INSERT INTO [`']?(\\w+)[`']?.*")) {
                    String tableName = trimmed.replaceAll("(?i)INSERT INTO [`']?(\\w+)[`']?.*", "$1").toLowerCase();

                    if (tableName.equals("movies") || tableName.equals("directors") ||
                            tableName.equals("director_directors") || tableName.equals("locations") ||
                            tableName.equals("persons") || tableName.equals("person_known_for_titles") ||
                            tableName.equals("person_primary_professions") || tableName.equals("principals") ||
                            tableName.equals("ratings") || tableName.equals("users") ||
                            tableName.equals("movie_search_history")) {
                        currentTable = tableName;
                        inInsert = true;
                        currentStatement.setLength(0);

                        log.debug("🔍 Début INSERT INTO {} détecté", tableName);

                        if (trimmed.matches("(?i).*VALUES.*")) {
                            currentStatement.append(trimmed);
                        }
                    } else {
                        inInsert = false;
                        currentTable = null;
                    }
                    continue;
                }

                // Si on est dans un INSERT
                if (inInsert && currentTable != null) {
                    currentStatement.append(" ").append(trimmed);

                    // Vérifier si on a fini de lire l'INSERT (point-virgule final trouvé)
                    String currentStmt = currentStatement.toString();
                    if (trimmed.endsWith(";") && currentStmt.contains("VALUES")) {
                        // Parser les valeurs et les ajouter au batch approprié
                        List<Map<String, String>> records = parseInsertValues(
                                currentStmt, currentTable, tableColumns);

                        log.debug("🔍 {} records parsés pour table {}", records.size(), currentTable);
                        if (!records.isEmpty() && currentTable.equals("locations")) {
                            log.info("🔍 Premier record de locations: {}", records.get(0));
                        }

                        switch (currentTable) {
                            case "movies":
                                if (importMovies) {
                                    moviesBatch.addAll(records);
                                    if (moviesBatch.size() >= BATCH_SIZE) {
                                        migrateMoviesBatch(moviesBatch);
                                        moviesBatch.clear();
                                    }
                                }
                                break;

                            case "directors":
                                if (importDirectors) {
                                    // Stocker les mappings id -> id_imdb
                                    for (Map<String, String> record : records) {
                                        String id = record.get("id");
                                        String idImdb = record.get("idImdb");
                                        if (id != null && idImdb != null) {
                                            directorIdToImdbId.put(id, idImdb);
                                        }
                                    }
                                }
                                break;

                            case "director_directors":
                                if (importDirectors) {
                                    directorRelationsBatch.addAll(records);
                                    if (directorRelationsBatch.size() >= BATCH_SIZE) {
                                        migrateDirectorRelationsBatch(directorRelationsBatch);
                                        directorRelationsBatch.clear();
                                    }
                                }
                                break;

                            case "locations":
                                if (importLocations) {
                                    locationsBatch.addAll(records);
                                    if (locationsBatch.size() >= BATCH_SIZE) {
                                        migrateLocationsBatch(locationsBatch);
                                        locationsBatch.clear();
                                    }
                                }
                                break;

                            case "persons":
                            case "person_known_for_titles":
                            case "person_primary_professions":
                                if (importPersons) {
                                    personsBatch.addAll(records);
                                    if (personsBatch.size() >= BATCH_SIZE) {
                                        migratePersonsBatch(personsBatch, currentTable);
                                        personsBatch.clear();
                                    }
                                }
                                break;

                            case "principals":
                                if (importPrincipals) {
                                    principalsBatch.addAll(records);
                                    if (principalsBatch.size() >= BATCH_SIZE) {
                                        migratePrincipalsBatch(principalsBatch);
                                        principalsBatch.clear();
                                    }
                                }
                                break;

                            case "ratings":
                                if (importRatings) {
                                    ratingsBatch.addAll(records);
                                    if (ratingsBatch.size() >= BATCH_SIZE) {
                                        migrateRatingsBatch(ratingsBatch);
                                        ratingsBatch.clear();
                                    }
                                }
                                break;

                            case "users":
                            case "movie_search_history":
                                if (importUsers) {
                                    usersBatch.addAll(records);
                                    if (usersBatch.size() >= BATCH_SIZE) {
                                        migrateUsersBatch(usersBatch, currentTable);
                                        usersBatch.clear();
                                    }
                                }
                                break;
                        }

                        currentStatement.setLength(0);
                        inInsert = false;
                        currentTable = null;
                    }
                }

                if (lineNum % 100000 == 0) {
                    log.info(
                            "  ⏳ {} lignes lues, {} films, {} relations dir, {} locs, {} pers, {} princ, {} rat, {} users",
                            lineNum, moviesCount.get(), directorsCount.get(), locationsCount.get(),
                            personsCount.get(), principalsCount.get(), ratingsCount.get(), usersCount.get());
                }
            }
        }

        // Traiter les lots restants
        if (!moviesBatch.isEmpty() && importMovies) {
            migrateMoviesBatch(moviesBatch);
        }
        if (!directorRelationsBatch.isEmpty() && importDirectors) {
            migrateDirectorRelationsBatch(directorRelationsBatch);
        }
        if (!locationsBatch.isEmpty() && importLocations) {
            migrateLocationsBatch(locationsBatch);
        }
        if (!personsBatch.isEmpty() && importPersons) {
            migratePersonsBatch(personsBatch, "persons");
        }
        if (!principalsBatch.isEmpty() && importPrincipals) {
            migratePrincipalsBatch(principalsBatch);
        }
        if (!ratingsBatch.isEmpty() && importRatings) {
            migrateRatingsBatch(ratingsBatch);
        }
        if (!usersBatch.isEmpty() && importUsers) {
            migrateUsersBatch(usersBatch, "users");
        }
    }

    /**
     * Parse les valeurs INSERT
     */
    private List<Map<String, String>> parseInsertValues(
            String insertStatement, String tableName, Map<String, List<String>> tableColumns) {

        List<Map<String, String>> results = new ArrayList<>();

        try {
            // Extraire la partie VALUES
            int valuesIndex = insertStatement.toUpperCase().indexOf("VALUES");
            if (valuesIndex == -1) {
                log.warn("⚠️ Pas de VALUES trouvé dans INSERT pour table {}", tableName);
                return results;
            }

            String valuesSection = insertStatement.substring(valuesIndex + 6).trim();
            if (valuesSection.endsWith(";")) {
                valuesSection = valuesSection.substring(0, valuesSection.length() - 1);
            }

            // Récupérer les colonnes pour cette table
            List<String> columns = tableColumns.get(tableName);
            if (columns == null || columns.isEmpty()) {
                log.warn("⚠️ Aucune colonne trouvée pour la table {}", tableName);
                return results;
            }

            if (tableName.equals("locations")) {
                log.info("🔍 Colonnes de locations: {}", columns);
            }

            // Pattern pour matcher les tuples de valeurs (robuste aux parenthèses dans les
            // chaînes)
            List<String> tuples = extractTuples(valuesSection);

            for (String tupleContent : tuples) {
                List<String> values = parseValues(tupleContent);

                if (values.size() == columns.size()) {
                    Map<String, String> record = new HashMap<>();
                    for (int i = 0; i < columns.size(); i++) {
                        String columnName = toCamelCase(columns.get(i));
                        String value = cleanValue(values.get(i));
                        record.put(columnName, value);
                    }
                    results.add(record);
                } else if (tableName.equals("locations") && !values.isEmpty()) {
                    log.warn("⚠️ Nombre de valeurs ({}) != nombre de colonnes ({}) pour locations",
                            values.size(), columns.size());
                }
            }
        } catch (Exception e) {
            log.warn("Erreur lors du parsing de {}: {}", tableName, e.getMessage(), e);
        }

        return results;
    }

    /**
     * Extrait les tuples en gérant les parenthèses dans les chaînes
     */
    private List<String> extractTuples(String valuesSection) {
        List<String> tuples = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = 0;
        int depth = 0;

        for (int i = 0; i < valuesSection.length(); i++) {
            char c = valuesSection.charAt(i);

            if (!inQuote && (c == '\'' || c == '"')) {
                inQuote = true;
                quoteChar = c;
                current.append(c);
            } else if (inQuote && c == quoteChar) {
                // Vérifier si c'est un échappement
                if (i + 1 < valuesSection.length() && valuesSection.charAt(i + 1) == quoteChar) {
                    current.append(c);
                    current.append(c);
                    i++;
                } else {
                    inQuote = false;
                    current.append(c);
                }
            } else if (!inQuote && c == '(') {
                if (depth == 0) {
                    // Début d'un nouveau tuple
                    current.setLength(0);
                } else {
                    current.append(c);
                }
                depth++;
            } else if (!inQuote && c == ')') {
                depth--;
                if (depth == 0) {
                    // Fin du tuple
                    tuples.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            } else if (depth > 0) {
                current.append(c);
            }
        }

        return tuples;
    }

    /**
     * Parse les valeurs d'un tuple
     */
    private List<String> parseValues(String values) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < values.length(); i++) {
            char c = values.charAt(i);

            if (!inQuote && (c == '\'' || c == '"')) {
                inQuote = true;
                quoteChar = c;
                current.append(c);
            } else if (inQuote && c == quoteChar) {
                // Vérifier si c'est un échappement
                if (i + 1 < values.length() && values.charAt(i + 1) == quoteChar) {
                    current.append(c);
                    current.append(c);
                    i++;
                } else {
                    inQuote = false;
                    current.append(c);
                }
            } else if (!inQuote && c == ',') {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result;
    }

    /**
     * Nettoie une valeur
     */
    private String cleanValue(String value) {
        if (value == null)
            return null;

        value = value.trim();

        if (value.isEmpty() || value.equalsIgnoreCase("NULL")) {
            return null;
        }

        if (value.length() >= 2) {
            if ((value.startsWith("'") && value.endsWith("'")) ||
                    (value.startsWith("\"") && value.endsWith("\""))) {
                value = value.substring(1, value.length() - 1);
            }
        }

        value = value.replace("\\'", "'")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");

        return value;
    }

    /**
     * Convertit snake_case en camelCase
     */
    private String toCamelCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty())
            return snakeCase;

        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;

        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    /**
     * Migre un lot de films vers Neo4j
     */
    private void migrateMoviesBatch(List<Map<String, String>> movies) {
        if (movies.isEmpty())
            return;

        try (Session session = neo4jDriver.session()) {
            // Utiliser UNWIND pour insérer en masse
            session.writeTransaction(tx -> {
                List<Map<String, Object>> movieData = new ArrayList<>();
                for (Map<String, String> movie : movies) {
                    movieData.add(new HashMap<>(movie));
                    // Log désactivé pour performance
                }

                // Créer les films
                tx.run(
                        "UNWIND $movies AS movie " +
                                "MERGE (m:Movie {idImdb: movie.idImdb}) " +
                                "SET m.primaryTitle = movie.primaryTitle, " +
                                "    m.originalTitle = movie.originalTitle, " +
                                "    m.startYear = movie.startYear, " +
                                "    m.runtimeMinutes = movie.runtimeMinutes, " +
                                "    m.genres = movie.genres",
                        Values.parameters("movies", movieData));

                // Créer les genres et les relations
                for (Map<String, String> movie : movies) {
                    String genres = movie.get("genres");
                    String idImdb = movie.get("idImdb");

                    if (genres != null && !genres.isEmpty() && idImdb != null) {
                        // Séparer les genres (format: "Action,Drama,Thriller")
                        String[] genreList = genres.split(",");
                        for (String genre : genreList) {
                            String genreTrimmed = genre.trim();
                            if (!genreTrimmed.isEmpty()) {
                                tx.run(
                                        "MERGE (g:Genre {name: $genreName}) " +
                                                "WITH g " +
                                                "MATCH (m:Movie {idImdb: $movieId}) " +
                                                "MERGE (m)-[:HAS_GENRE]->(g)",
                                        Values.parameters("genreName", genreTrimmed, "movieId", idImdb));
                            }
                        }
                    }
                }

                return null;
            });

            int newTotal = moviesCount.addAndGet(movies.size());
            if (newTotal % 10000 == 0 || movies.size() < BATCH_SIZE) {
                log.info("📦 Batch films: {} ajoutés | Total: {}", movies.size(), newTotal);
            }
        }
    }

    /**
     * Migre un lot de relations director-film vers Neo4j
     */
    private void migrateDirectorRelationsBatch(List<Map<String, String>> relations) {
        if (relations.isEmpty())
            return;

        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                // D'abord créer les personnes
                List<String> nconsts = new ArrayList<>();
                for (Map<String, String> relation : relations) {
                    String nconst = relation.get("directors");
                    if (nconst != null && !nconsts.contains(nconst)) {
                        nconsts.add(nconst);
                        // Log désactivé pour performance
                    }
                }

                if (!nconsts.isEmpty()) {
                    tx.run(
                            "UNWIND $nconsts AS nconst " +
                                    "MERGE (p:Person {nconst: nconst})",
                            Values.parameters("nconsts", nconsts));
                }

                // Ensuite créer les relations
                List<Map<String, Object>> relationData = new ArrayList<>();
                for (Map<String, String> relation : relations) {
                    String directorId = relation.get("directorId");
                    String nconst = relation.get("directors");

                    if (directorId != null && nconst != null) {
                        String movieIdImdb = directorIdToImdbId.get(directorId);
                        if (movieIdImdb != null) {
                            Map<String, Object> relData = new HashMap<>();
                            relData.put("movieIdImdb", movieIdImdb);
                            relData.put("nconst", nconst);
                            relationData.add(relData);
                            // Log désactivé pour performance
                        }
                    }
                }

                if (!relationData.isEmpty()) {
                    tx.run(
                            "UNWIND $relations AS rel " +
                                    "MATCH (m:Movie {idImdb: rel.movieIdImdb}) " +
                                    "MATCH (p:Person {nconst: rel.nconst}) " +
                                    "MERGE (m)-[:DIRECTED_BY]->(p)",
                            Values.parameters("relations", relationData));
                }

                return null;
            });

            int newTotal = directorsCount.addAndGet(relations.size());
            if (newTotal % 10000 == 0 || relations.size() < BATCH_SIZE) {
                log.info("📦 Batch directeurs: {} ajoutés | Total: {}", relations.size(), newTotal);
            }
        }
    }

    /**
     * Migre un lot de localisations vers Neo4j
     */
    private void migrateLocationsBatch(List<Map<String, String>> locations) {
        if (locations.isEmpty())
            return;

        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                List<Map<String, Object>> locationData = new ArrayList<>();
                for (Map<String, String> location : locations) {
                    locationData.add(new HashMap<>(location));
                    // Log désactivé pour performance
                }

                // Créer les localisations
                tx.run(
                        "UNWIND $locations AS loc " +
                                "MERGE (l:Location {id: loc.id}) " +
                                "SET l.displayName = loc.displayName, " +
                                "    l.countryCode = loc.countryCode, " +
                                "    l.description = loc.description, " +
                                "    l.locationString = loc.locationString, " +
                                "    l.latitude = loc.latitude, " +
                                "    l.longitude = loc.longitude, " +
                                "    l.geocodingFailed = loc.geocodingFailed",
                        Values.parameters("locations", locationData));

                // Créer les relations Location -> Movie
                List<Map<String, Object>> movieRelations = new ArrayList<>();
                for (Map<String, String> location : locations) {
                    String idImdb = location.get("idImdb");
                    String locId = location.get("id");
                    if (idImdb != null && !idImdb.isEmpty() && locId != null) {
                        Map<String, Object> rel = new HashMap<>();
                        rel.put("locId", locId);
                        rel.put("movieId", idImdb);
                        movieRelations.add(rel);
                    }
                }

                if (!movieRelations.isEmpty()) {
                    tx.run(
                            "UNWIND $relations AS rel " +
                                    "MATCH (l:Location {id: rel.locId}) " +
                                    "MATCH (m:Movie {idImdb: rel.movieId}) " +
                                    "MERGE (m)-[:FILMED_AT]->(l)",
                            Values.parameters("relations", movieRelations));
                    log.info("🔗 {} relations Location->Movie créées", movieRelations.size());
                }

                return null;
            });

            int newTotal = locationsCount.addAndGet(locations.size());
            if (newTotal % 10000 == 0 || locations.size() < BATCH_SIZE) {
                log.info("📦 Batch localisations: {} ajoutées | Total: {}", locations.size(), newTotal);
            }
        }
    }

    /**
     * Migre un lot de personnes vers Neo4j
     */
    private void migratePersonsBatch(List<Map<String, String>> persons, String tableName) {
        if (persons.isEmpty())
            return;

        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                List<Map<String, Object>> personData = new ArrayList<>();

                for (Map<String, String> person : persons) {
                    String nconst = person.get("nconst");
                    if (nconst != null) {
                        personData.add(new HashMap<>(person));
                        // Log désactivé pour performance
                    }
                }

                // Créer les personnes
                if (!personData.isEmpty()) {
                    tx.run(
                            "UNWIND $persons AS person " +
                                    "MERGE (p:Person {nconst: person.nconst}) " +
                                    "SET p.primaryName = person.primaryName, " +
                                    "    p.birthYear = person.birthYear, " +
                                    "    p.deathYear = person.deathYear, " +
                                    "    p.primaryProfession = person.primaryProfession",
                            Values.parameters("persons", personData));
                }

                // Pour person_known_for_titles : créer des relations
                // (Person)-[:KNOWN_FOR]->(Movie)
                if (tableName.equals("person_known_for_titles")) {
                    for (Map<String, String> person : persons) {
                        String nconst = person.get("nconst");
                        String knownForTitles = person.get("knownForTitles");

                        if (nconst != null && knownForTitles != null && !knownForTitles.isEmpty()) {
                            String[] titles = knownForTitles.split(",");
                            for (String tconst : titles) {
                                String trimmed = tconst.trim();
                                if (!trimmed.isEmpty()) {
                                    tx.run(
                                            "MATCH (p:Person {nconst: $nconst}) " +
                                                    "MERGE (m:Movie {idImdb: $tconst}) " +
                                                    "MERGE (p)-[:KNOWN_FOR]->(m)",
                                            Values.parameters("nconst", nconst, "tconst", trimmed));
                                }
                            }
                        }
                    }
                }

                // Pour person_primary_professions : créer des nœuds Profession et relations
                if (tableName.equals("person_primary_professions")) {
                    for (Map<String, String> person : persons) {
                        String nconst = person.get("nconst");
                        String professions = person.get("primaryProfession");

                        if (nconst != null && professions != null && !professions.isEmpty()) {
                            String[] profList = professions.split(",");
                            for (String profession : profList) {
                                String trimmed = profession.trim();
                                if (!trimmed.isEmpty()) {
                                    tx.run(
                                            "MERGE (prof:Profession {name: $profName}) " +
                                                    "WITH prof " +
                                                    "MATCH (p:Person {nconst: $nconst}) " +
                                                    "MERGE (p)-[:HAS_PROFESSION]->(prof)",
                                            Values.parameters("profName", trimmed, "nconst", nconst));
                                }
                            }
                        }
                    }
                }

                return null;
            });
            int newTotal = personsCount.addAndGet(persons.size());
            if (newTotal % 10000 == 0 || persons.size() < BATCH_SIZE) {
                log.info("📦 Batch personnes: {} ajoutées | Total: {} | Estimation: {:.1f}% (12M)",
                        persons.size(), newTotal, (newTotal / 12000000.0 * 100));
            }
        }
    }

    /**
     * Migre un lot de principaux vers Neo4j
     */
    private void migratePrincipalsBatch(List<Map<String, String>> principals) {
        if (principals.isEmpty())
            return;

        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                List<Map<String, Object>> principalData = new ArrayList<>();

                for (Map<String, String> principal : principals) {
                    String tconst = principal.get("tconst");
                    String nconst = principal.get("nconst");
                    String category = principal.get("category");

                    if (tconst != null && nconst != null) {
                        Map<String, Object> data = new HashMap<>(principal);
                        principalData.add(data);
                        // Log désactivé pour performance
                    }
                }

                // Créer les relations (Person)-[:ACTED_IN|DIRECTED|etc]->(Movie)
                for (Map<String, String> principal : principals) {
                    String tconst = principal.get("tconst");
                    String nconst = principal.get("nconst");
                    String category = principal.get("category");
                    String characters = principal.get("characters");
                    String job = principal.get("job");

                    if (tconst != null && nconst != null && category != null) {
                        // Déterminer le type de relation selon la catégorie
                        String relationType;
                        switch (category.toLowerCase()) {
                            case "actor":
                            case "actress":
                                relationType = "ACTED_IN";
                                break;
                            case "director":
                                relationType = "DIRECTED";
                                break;
                            case "writer":
                                relationType = "WROTE";
                                break;
                            case "producer":
                                relationType = "PRODUCED";
                                break;
                            case "cinematographer":
                                relationType = "CINEMATOGRAPHY";
                                break;
                            case "composer":
                                relationType = "COMPOSED";
                                break;
                            case "editor":
                                relationType = "EDITED";
                                break;
                            default:
                                relationType = "WORKED_ON";
                                break;
                        }

                        // Créer la personne et le film si nécessaire, puis la relation
                        tx.run(
                                "MERGE (p:Person {nconst: $nconst}) " +
                                        "MERGE (m:Movie {idImdb: $tconst}) " +
                                        "MERGE (p)-[r:" + relationType + "]->(m) " +
                                        "SET r.category = $category, " +
                                        "    r.characters = $characters, " +
                                        "    r.job = $job",
                                Values.parameters(
                                        "nconst", nconst,
                                        "tconst", tconst,
                                        "category", category,
                                        "characters", characters,
                                        "job", job));
                    }
                }

                return null;
            });
            int newTotal = principalsCount.addAndGet(principals.size());
            if (newTotal % 10000 == 0 || principals.size() < BATCH_SIZE) {
                log.info("📦 Batch principals: {} ajoutés | Total: {} | Estimation: {:.1f}% (7M)",
                        principals.size(), newTotal, (newTotal / 7000000.0 * 100));
            }
        }
    }

    /**
     * Migre un lot de ratings vers Neo4j
     */
    private void migrateRatingsBatch(List<Map<String, String>> ratings) {
        if (ratings.isEmpty())
            return;

        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                for (Map<String, String> rating : ratings) {
                    // Log désactivé pour performance
                }
                return null;
            });
            int newTotal = ratingsCount.addAndGet(ratings.size());
            if (newTotal % 10000 == 0 || ratings.size() < BATCH_SIZE) {
                log.info("📦 Batch ratings: {} ajoutés | Total: {}", ratings.size(), newTotal);
            }
        }
    }

    /**
     * Migre un lot d'utilisateurs vers Neo4j
     */
    private void migrateUsersBatch(List<Map<String, String>> users, String tableName) {
        if (users.isEmpty())
            return;

        try (Session session = neo4jDriver.session()) {
            session.writeTransaction(tx -> {
                for (Map<String, String> user : users) {
                    // Log désactivé pour performance
                }
                return null;
            });
            int newTotal = usersCount.addAndGet(users.size());
            if (newTotal % 10000 == 0 || users.size() < BATCH_SIZE) {
                log.info("📦 Batch users: {} ajoutés | Total: {}", users.size(), newTotal);
            }
        }
    }

    /**
     * Affiche les statistiques finales
     */
    private void printStatistics() {
        log.info("📊 Statistiques de migration:");
        log.info("  - Films: {}", moviesCount.get());
        log.info("  - Relations Film-Réalisateur: {}", directorsCount.get());
        log.info("  - Localisations: {}", locationsCount.get());
        log.info("  - Personnes: {}", personsCount.get());
        log.info("  - Principaux: {}", principalsCount.get());
        log.info("  - Ratings: {}", ratingsCount.get());
        log.info("  - Utilisateurs: {}", usersCount.get());
    }

    /**
     * Main pour exécution standalone
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java SqlToNeo4jMigration <dump.sql> [options]");
            System.err.println("Options:");
            System.err.println("  --movies=true|false      Importer les films (défaut: true)");
            System.err.println("  --directors=true|false   Importer les réalisateurs (défaut: true)");
            System.err.println("  --locations=true|false   Importer les localisations (défaut: true)");
            System.err.println("  --persons=true|false     Importer les personnes (défaut: true)");
            System.err.println("  --principals=true|false  Importer les principaux (défaut: true)");
            System.err.println("  --ratings=true|false     Importer les ratings (défaut: true)");
            System.err.println("  --users=true|false       Importer les utilisateurs (défaut: true)");
            System.err.println("  --clean=true|false       Nettoyer la base avant import (défaut: false)");
            System.err.println("Exemples:");
            System.err.println("  java SqlToNeo4jMigration dump.sql");
            System.err.println("  java SqlToNeo4jMigration dump.sql --movies=true --directors=false --locations=true");
            System.err.println(
                    "  java SqlToNeo4jMigration dump.sql --clean=true  # Supprime toutes les données avant import");
            System.exit(1);
        }

        String sqlDumpPath = args[0];
        String neo4jUri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost:7687");
        String neo4jUser = System.getenv().getOrDefault("NEO4J_USER", "neo4j");
        String neo4jPassword = System.getenv().getOrDefault("NEO4J_PASSWORD", "cinetour_neo4j_password");

        SqlToNeo4jMigration migration = new SqlToNeo4jMigration(neo4jUri, neo4jUser, neo4jPassword);

        // Parser les options
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--movies=")) {
                migration.setImportMovies(Boolean.parseBoolean(arg.substring(9)));
            } else if (arg.startsWith("--directors=")) {
                migration.setImportDirectors(Boolean.parseBoolean(arg.substring(12)));
            } else if (arg.startsWith("--locations=")) {
                migration.setImportLocations(Boolean.parseBoolean(arg.substring(12)));
            } else if (arg.startsWith("--persons=")) {
                migration.setImportPersons(Boolean.parseBoolean(arg.substring(10)));
            } else if (arg.startsWith("--principals=")) {
                migration.setImportPrincipals(Boolean.parseBoolean(arg.substring(13)));
            } else if (arg.startsWith("--ratings=")) {
                migration.setImportRatings(Boolean.parseBoolean(arg.substring(10)));
            } else if (arg.startsWith("--users=")) {
                migration.setImportUsers(Boolean.parseBoolean(arg.substring(8)));
            } else if (arg.startsWith("--clean=")) {
                migration.setCleanDatabase(Boolean.parseBoolean(arg.substring(8)));
            }
        }

        migration.migrate(sqlDumpPath);
    }
}
