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

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polytech.crud.neo4j.entity.MovieNode;
import com.polytech.crud.neo4j.entity.PersonNode;
import com.polytech.crud.neo4j.repository.MovieNodeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!import")
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
    public List<MovieNode> getRecommendationsByDirectors(String idImdb, double minRating, int limit) {
        log.info("Recherche de recommandations par directeurs pour le film: {} (rating min: {})", idImdb, minRating);
        List<MovieNode> movies = movieNodeRepository.findMoviesBySameDirectors(idImdb, minRating, limit);
        return enrichMoviesWithDetails(movies);
    }

    /**
     * Recommandations basées sur les acteurs communs
     */
    public List<MovieNode> getRecommendationsByActors(String idImdb, int limit) {
        log.info("Recherche de recommandations par acteurs pour le film: {}", idImdb);
        List<MovieNode> movies = movieNodeRepository.findMoviesBySameActors(idImdb, limit);
        return enrichMoviesWithDetails(movies);
    }

    /**
     * Recommandations basées sur le genre
     */
    public List<MovieNode> getRecommendationsByGenre(String idImdb, double minRating, int limit) {
        log.info("Recherche de recommandations par genre pour le film: {} (rating min: {})", idImdb, minRating);
        List<MovieNode> movies = movieNodeRepository.findMoviesBySameGenre(idImdb, minRating, limit * 3);
        return sortByPopularityScore(movies, limit);
    }

    /**
     * Recommandations basées sur l'époque et le genre
     */
    public List<MovieNode> getRecommendationsByEraAndGenre(String idImdb, int yearRange, double minRating, int limit) {
        log.info("Recherche de recommandations par époque et genre pour le film: {} (±{} ans, rating min: {})",
                idImdb, yearRange, minRating);
        List<MovieNode> movies = movieNodeRepository.findMoviesBySameEraAndGenre(idImdb, yearRange, minRating,
                limit * 3);
        return sortByPopularityScore(movies, limit);
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

                    // Charger les principals (acteurs) avec leurs informations complètes
                    List<com.polytech.crud.neo4j.dto.PersonProjection> principalsData = movieNodeRepository
                            .findPrincipalsByMovieIdImdb(movie.getIdImdb());

                    // Créer des PrincipalRelationship avec PersonNode complet
                    Set<com.polytech.crud.neo4j.entity.PrincipalRelationship> principals = principalsData.stream()
                            .filter(data -> data.getNconst() != null)
                            .map(data -> {
                                com.polytech.crud.neo4j.entity.PersonNode person = new com.polytech.crud.neo4j.entity.PersonNode();
                                person.setNconst(data.getNconst());
                                person.setPrimaryName(data.getPrimaryName());
                                person.setBirthYear(data.getBirthYear());
                                person.setDeathYear(data.getDeathYear());

                                com.polytech.crud.neo4j.entity.PrincipalRelationship principal = new com.polytech.crud.neo4j.entity.PrincipalRelationship();
                                principal.setPerson(person);
                                return principal;
                            })
                            .collect(java.util.stream.Collectors.toSet());

                    movie.setPrincipals(principals);

                    // Calculer le score de similarité en Java
                    int score = calculateSimilarityScore(referenceMovie, movie);

                    // Calculer le score de popularité (note * nombre de votes)
                    double popularityScore = calculatePopularityScore(movie, score);

                    // Nettoyer les directors et principals avant de renvoyer au front
                    movie.setDirectors(new java.util.HashSet<>());
                    movie.setPrincipals(new java.util.HashSet<>());

                    Map<String, Object> result = new HashMap<>();
                    result.put("movie", movie);
                    result.put("rating", createDefaultRating(movie));
                    result.put("similarityScore", score);
                    result.put("popularityScore", popularityScore);

                    return result;
                })
                .filter(result -> (int) result.get("similarityScore") > 0) // Filtrer les films sans similarité
                .sorted((a, b) -> {
                    // Tri par score de popularité décroissant (intègre similarité + popularité du
                    // film)
                    double scoreA = (double) a.get("popularityScore");
                    double scoreB = (double) b.get("popularityScore");
                    return Double.compare(scoreB, scoreA);
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
     * Calcule le score de popularité combinant la similarité et la popularité du
     * film
     * Utilise un coefficient basé sur note * nombre de votes pour favoriser les
     * films connus
     * 
     * @param movie           Le film candidat
     * @param similarityScore Le score de similarité de base
     * @return Le score de popularité pondéré
     */
    private double calculatePopularityScore(MovieNode movie, int similarityScore) {
        // Si pas de rating, retourner juste le score de similarité
        if (movie.getAverageRating() == null || movie.getNumVotes() == null) {
            return (double) similarityScore;
        }

        try {
            // Convertir numVotes de String vers long
            long numVotes = Long.parseLong(movie.getNumVotes());

            // Coefficient de popularité : (note * nombre de votes) / 1000
            // Division par 1000 pour normaliser les valeurs
            double popularityCoefficient = (movie.getAverageRating() * numVotes) / 1000.0;

            // Score final = score de similarité * coefficient de popularité
            // Cela privilégie les films avec de bonnes notes ET beaucoup de votes
            return similarityScore * popularityCoefficient;
        } catch (NumberFormatException e) {
            // En cas d'erreur de conversion, retourner juste le score de similarité
            log.warn("Impossible de convertir numVotes pour le film {}: {}", movie.getIdImdb(), movie.getNumVotes());
            return (double) similarityScore;
        }
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
     * Enrichit une liste de films avec les informations de directors et principals
     * 
     * @param movies Liste des films à enrichir
     * @return Liste des films enrichis
     */
    private List<MovieNode> enrichMoviesWithDetails(List<MovieNode> movies) {
        return movies.stream()
                .map(movie -> {
                    // Charger les nconst des directors
                    List<String> directorNconsts = movieNodeRepository
                            .findDirectorNconstsByMovieIdImdb(movie.getIdImdb());

                    // Créer des PersonNode avec seulement le nconst
                    Set<com.polytech.crud.neo4j.entity.PersonNode> directors = directorNconsts.stream()
                            .filter(nconst -> nconst != null && !nconst.isEmpty())
                            .map(nconst -> {
                                com.polytech.crud.neo4j.entity.PersonNode person = new com.polytech.crud.neo4j.entity.PersonNode();
                                person.setNconst(nconst);
                                return person;
                            })
                            .collect(java.util.stream.Collectors.toSet());

                    movie.setDirectors(directors);

                    // Charger les principals (acteurs) avec leurs informations complètes
                    List<com.polytech.crud.neo4j.dto.PersonProjection> principalsData = movieNodeRepository
                            .findPrincipalsByMovieIdImdb(movie.getIdImdb());

                    // Créer des PrincipalRelationship avec PersonNode complet
                    Set<com.polytech.crud.neo4j.entity.PrincipalRelationship> principals = principalsData.stream()
                            .filter(data -> data.getNconst() != null)
                            .map(data -> {
                                com.polytech.crud.neo4j.entity.PersonNode person = new com.polytech.crud.neo4j.entity.PersonNode();
                                person.setNconst(data.getNconst());
                                person.setPrimaryName(data.getPrimaryName());
                                person.setBirthYear(data.getBirthYear());
                                person.setDeathYear(data.getDeathYear());

                                com.polytech.crud.neo4j.entity.PrincipalRelationship principal = new com.polytech.crud.neo4j.entity.PrincipalRelationship();
                                principal.setPerson(person);
                                return principal;
                            })
                            .collect(java.util.stream.Collectors.toSet());

                    movie.setPrincipals(principals);

                    return movie;
                })
                .collect(Collectors.toList());
    }

    /**
     * Trie les films par score de popularité (rating * numVotes) et limite les
     * résultats
     * 
     * @param movies Liste des films à trier
     * @param limit  Nombre maximum de résultats
     * @return Liste triée et limitée
     */
    private List<MovieNode> sortByPopularityScore(List<MovieNode> movies, int limit) {
        return movies.stream()
                .sorted((m1, m2) -> {
                    double score1 = getPopularityScore(m1);
                    double score2 = getPopularityScore(m2);
                    return Double.compare(score2, score1); // Ordre décroissant
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Calcule le score de popularité d'un film (rating * numVotes)
     * 
     * @param movie Le film à évaluer
     * @return Le score de popularité
     */
    private double getPopularityScore(MovieNode movie) {
        if (movie.getAverageRating() == null || movie.getNumVotes() == null) {
            return 0.0;
        }

        try {
            long numVotes = Long.parseLong(movie.getNumVotes());
            return movie.getAverageRating() * numVotes;
        } catch (NumberFormatException e) {
            log.warn("Impossible de convertir numVotes pour le film {}: {}", movie.getIdImdb(), movie.getNumVotes());
            return 0.0;
        }
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
