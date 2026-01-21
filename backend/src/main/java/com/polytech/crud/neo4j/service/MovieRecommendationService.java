package com.polytech.crud.neo4j.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polytech.crud.neo4j.entity.MovieNode;
import com.polytech.crud.neo4j.entity.PersonNode;
import com.polytech.crud.neo4j.repository.MovieNodeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieRecommendationService {

    private final MovieNodeRepository movieNodeRepository;

    /**
     * DTO pour encapsuler un film recommandé avec son score
     */
    public static class RecommendedMovie {
        private final MovieNode movie;
        private final double similarityScore;
        private final String recommendationReason;

        public RecommendedMovie(MovieNode movie, double similarityScore, String reason) {
            this.movie = movie;
            this.similarityScore = similarityScore;
            this.recommendationReason = reason;
        }

        public MovieNode getMovie() {
            return movie;
        }

        public double getSimilarityScore() {
            return similarityScore;
        }

        public String getRecommendationReason() {
            return recommendationReason;
        }
    }

    /**
     * Recommandations basées sur les directeurs communs
     */
    public List<MovieNode> getRecommendationsByDirectors(String idImdb, int limit) {
        log.info("Recherche de recommandations par directeurs pour le film: {}", idImdb);
        return movieNodeRepository.findMoviesBySameDirectors(idImdb, limit);
    }

    /**
     * Recommandations basées sur les acteurs communs
     */
    public List<MovieNode> getRecommendationsByActors(String idImdb, int limit) {
        log.info("Recherche de recommandations par acteurs pour le film: {}", idImdb);
        return movieNodeRepository.findMoviesBySameActors(idImdb, limit);
    }

    /**
     * Recommandations basées sur le genre
     */
    public List<MovieNode> getRecommendationsByGenre(String idImdb, double minRating, int limit) {
        log.info("Recherche de recommandations par genre pour le film: {} (rating min: {})", idImdb, minRating);
        return movieNodeRepository.findMoviesBySameGenre(idImdb, minRating, limit);
    }

    /**
     * Recommandations basées sur l'époque et le genre
     */
    public List<MovieNode> getRecommendationsByEraAndGenre(String idImdb, int yearRange, double minRating, int limit) {
        log.info("Recherche de recommandations par époque et genre pour le film: {} (±{} ans, rating min: {})",
                idImdb, yearRange, minRating);
        return movieNodeRepository.findMoviesBySameEraAndGenre(idImdb, yearRange, minRating, limit);
    }

    /**
     * Recommandations combinées avec scoring avancé
     * Combine plusieurs critères : directeurs, acteurs, genres
     */
    public List<Map<String, Object>> getSmartRecommendations(String idImdb, Double minRating, Integer limit) {
        double effectiveMinRating = minRating != null ? minRating : 0.0;
        int effectiveLimit = limit != null ? limit : 20;

        log.info("Recherche de recommandations intelligentes pour le film: {} (rating min: {}, limit: {})",
                idImdb, effectiveMinRating, effectiveLimit);

        // Récupérer le film de référence avec ses directeurs
        MovieNode referenceMovie = movieNodeRepository.findByIdImdbForRecommendations(idImdb)
                .orElseThrow(() -> new RuntimeException("Film non trouvé: " + idImdb));

        // Charger manuellement les directors du film de référence
        List<String> refDirectorNconsts = movieNodeRepository.findDirectorNconstsByMovieIdImdb(idImdb);
        Set<com.polytech.crud.neo4j.entity.PersonNode> refDirectors = refDirectorNconsts.stream()
                .filter(nconst -> nconst != null && !nconst.isEmpty())
                .map(nconst -> {
                    com.polytech.crud.neo4j.entity.PersonNode person = new com.polytech.crud.neo4j.entity.PersonNode();
                    person.setNconst(nconst);
                    return person;
                })
                .collect(java.util.stream.Collectors.toSet());
        referenceMovie.setDirectors(refDirectors);

        log.info("Film de référence: {} avec {} directors", referenceMovie.getTitle(), refDirectors.size());

        // Récupérer directement les films candidats
        List<MovieNode> candidateMovies = movieNodeRepository.findRecommendedMoviesComplex(
                idImdb,
                effectiveMinRating,
                effectiveLimit * 3 // Récupérer plus de candidats pour avoir assez après filtrage
        );

        // Charger manuellement les directors pour chaque film candidat et calculer le
        // score
        return candidateMovies.stream()
                .map(movie -> {
                    // Charger les nconst des directors
                    List<String> directorNconsts = movieNodeRepository
                            .findDirectorNconstsByMovieIdImdb(movie.getIdImdb());

                    // Créer des PersonNode avec seulement le nconst (suffisant pour la comparaison)
                    Set<com.polytech.crud.neo4j.entity.PersonNode> directors = directorNconsts.stream()
                            .filter(nconst -> nconst != null && !nconst.isEmpty())
                            .map(nconst -> {
                                com.polytech.crud.neo4j.entity.PersonNode person = new com.polytech.crud.neo4j.entity.PersonNode();
                                person.setNconst(nconst);
                                return person;
                            })
                            .collect(java.util.stream.Collectors.toSet());

                    movie.setDirectors(directors);

                    Map<String, Object> result = new HashMap<>();
                    result.put("movie", movie);
                    result.put("rating", createDefaultRating(movie));

                    // Calculer le score de similarité en Java
                    int score = calculateSimilarityScore(referenceMovie, movie);
                    result.put("similarityScore", score);

                    return result;
                })
                .filter(result -> (int) result.get("similarityScore") > 0) // Filtrer les films sans similarité
                .sorted((a, b) -> {
                    // Tri par score de similarité décroissant
                    int scoreA = (int) a.get("similarityScore");
                    int scoreB = (int) b.get("similarityScore");
                    return Integer.compare(scoreB, scoreA);
                })
                .limit(effectiveLimit)
                .collect(Collectors.toList());
    }

    /**
     * Calcule le score de similarité entre deux films
     * - 5 points par directeur commun
     * - 3 points si même genre
     */
    private int calculateSimilarityScore(MovieNode reference, MovieNode candidate) {
        int score = 0;

        // Score des directeurs communs (5 points par directeur)
        if (reference.getDirectors() != null && candidate.getDirectors() != null) {
            Set<String> refDirectors = reference.getDirectors().stream()
                    .map(PersonNode::getNconst)
                    .collect(Collectors.toSet());
            long commonDirectors = candidate.getDirectors().stream()
                    .filter(d -> refDirectors.contains(d.getNconst()))
                    .count();
            score += (int) commonDirectors * 5;
        }

        // Score de genre (3 points si au moins un genre commun)
        if (reference.getGenres() != null && candidate.getGenres() != null) {
            String[] refGenres = reference.getGenres().split(",");
            String candidateGenres = candidate.getGenres();
            for (String genre : refGenres) {
                if (candidateGenres.contains(genre.trim())) {
                    score += 3;
                    break; // Un seul bonus pour les genres
                }
            }
        }

        return score;
    }

    /**
     * Crée un objet rating seulement si les données existent
     */
    private Map<String, Object> createDefaultRating(MovieNode movie) {
        // Ne créer un objet rating que si au moins une valeur existe
        if (movie.getAverageRating() == null && movie.getNumVotes() == null) {
            return null;
        }

        Map<String, Object> rating = new HashMap<>();
        if (movie.getAverageRating() != null) {
            rating.put("averageRating", movie.getAverageRating());
        }
        if (movie.getNumVotes() != null) {
            rating.put("numVotes", movie.getNumVotes());
        }
        rating.put("idImdb", movie.getIdImdb());
        return rating;
    }

    /**
     * Méthode simplifiée qui retourne directement les films
     */
    public List<MovieNode> getRecommendedMovies(String idImdb, Double minRating, Integer limit) {
        List<Map<String, Object>> smartRecommendations = getSmartRecommendations(idImdb, minRating, limit);

        return smartRecommendations.stream()
                .map(map -> (MovieNode) map.get("movie"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Recommandations avec raisons détaillées
     */
    public List<Map<String, Object>> getDetailedRecommendations(String idImdb, Double minRating, Integer limit) {
        double effectiveMinRating = minRating != null ? minRating : 0.0;
        int effectiveLimit = limit != null ? limit : 20;

        log.info("Recherche de recommandations détaillées pour le film: {}", idImdb);

        // Vérifier que le film existe
        Optional<MovieNode> sourceMovie = movieNodeRepository.findByIdImdb(idImdb);
        if (sourceMovie.isEmpty()) {
            log.warn("Film source introuvable: {}", idImdb);
            return Collections.emptyList();
        }

        // Obtenir les recommandations intelligentes
        List<Map<String, Object>> recommendations = getSmartRecommendations(idImdb, effectiveMinRating, effectiveLimit);

        // Enrichir avec des raisons plus détaillées
        return recommendations.stream()
                .map(rec -> {
                    MovieNode movie = (MovieNode) rec.get("movie");
                    double score = (double) rec.get("similarityScore");

                    // Construire la raison basée sur le score
                    String reason = buildRecommendationReason(sourceMovie.get(), movie, score);

                    Map<String, Object> detailedRec = new HashMap<>(rec);
                    detailedRec.put("reason", reason);
                    detailedRec.put("imdbId", movie.getIdImdb());
                    detailedRec.put("title", movie.getTitle());
                    detailedRec.put("year", movie.getReleaseYear());

                    return detailedRec;
                })
                .collect(Collectors.toList());
    }

    /**
     * Construit une raison lisible pour la recommandation
     */
    private String buildRecommendationReason(MovieNode source, MovieNode recommended, double similarityScore) {
        List<String> reasons = new ArrayList<>();

        // Vérifier les directeurs communs
        if (source.getDirectors() != null && recommended.getDirectors() != null) {
            long commonDirectors = source.getDirectors().stream()
                    .filter(d -> recommended.getDirectors().stream()
                            .anyMatch(rd -> rd.getNconst().equals(d.getNconst())))
                    .count();
            if (commonDirectors > 0) {
                reasons.add("même réalisateur" + (commonDirectors > 1 ? "s" : ""));
            }
        }

        // Vérifier les genres communs
        if (source.getGenres() != null && recommended.getGenres() != null) {
            Set<String> sourceGenres = new HashSet<>(Arrays.asList(source.getGenres().split(",")));
            Set<String> recGenres = new HashSet<>(Arrays.asList(recommended.getGenres().split(",")));
            sourceGenres.retainAll(recGenres);
            if (!sourceGenres.isEmpty()) {
                reasons.add("genre similaire (" + String.join(", ", sourceGenres) + ")");
            }
        }

        // Vérifier la proximité temporelle
        if (source.getReleaseYear() != null && recommended.getReleaseYear() != null) {
            try {
                int sourceYear = Integer.parseInt(source.getReleaseYear());
                int recommendedYear = Integer.parseInt(recommended.getReleaseYear());
                int yearDiff = Math.abs(sourceYear - recommendedYear);
                if (yearDiff <= 3) {
                    reasons.add("même époque");
                }
            } catch (NumberFormatException e) {
                // Ignore si les années ne sont pas des nombres valides
            }
        }

        if (reasons.isEmpty()) {
            reasons.add("acteurs communs");
        }

        return String.join(", ", reasons) + " (score: " + String.format("%.1f", similarityScore) + ")";
    }

    /**
     * Obtenir uniquement les IDs IMDB des films recommandés
     */
    public List<String> getRecommendedMovieIds(String idImdb, Double minRating, Integer limit) {
        return getRecommendedMovies(idImdb, minRating, limit).stream()
                .map(MovieNode::getIdImdb)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
