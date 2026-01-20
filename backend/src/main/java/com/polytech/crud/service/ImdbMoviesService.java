package com.polytech.crud.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polytech.crud.dto.TmdbMovieInfo;
import com.polytech.crud.entity.Movie;
import com.polytech.crud.repository.MovieRepository;
import com.polytech.utils.ImdbDatasets;

import jakarta.persistence.EntityManager;

@Service
public class ImdbMoviesService {
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ImdbExtraction imdbExtraction;

    @Value("${tmdb.api.token:}")
    private String tmdbApiToken;

    private List<Movie> parseMoviesTsvFile(String tsvFilePath) throws IOException {
        List<Movie> movies = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(tsvFilePath))) {
            // Skip header
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                try {
                    String[] fields = line.split("\t");
                    if (fields.length >= 3 && "movie".equals(fields[1])) {
                        Movie movie = new Movie();
                        // Remove quotes and escape special characters
                        String title = fields[2].replaceAll("^\"|\"$", "");
                        title = StringEscapeUtils.escapeCsv(title);
                        movie.setTitle(title);
                        movie.setIdImdb(fields[0]);
                        if (!fields[5].equals("\\N")) {
                            movie.setReleaseYear(Integer.parseInt(fields[5]));
                        } else {
                            movie.setReleaseYear(null);
                        }
                        if (!fields[7].equals("\\N")) {
                            movie.setRuntimeMinutes(Integer.parseInt(fields[7]));
                        } else {
                            movie.setRuntimeMinutes(null);
                        }
                        if (!fields[8].equals("\\N")) {
                            movie.setGenres(fields[8]);
                        } else {
                            movie.setGenres(null);
                        }
                        movies.add(movie);
                    }
                } catch (Exception e) {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
        }
        return movies;
    }

    public List<Movie> getMovies() throws IOException {
        String gzFileName = ImdbDatasets.MOVIE_BASICS.getFileName();
        String tsvFileName = gzFileName.replace(".gz", "");
        try {
            imdbExtraction.downloadFile(ImdbDatasets.MOVIE_BASICS.getUrl(), gzFileName);
        } catch (Exception e) {
            System.out.println("Failed to download file: " + e.getMessage());
            return new ArrayList<>();
        }
        System.out.println("Extracting IMDb dataset");
        imdbExtraction.extractGzFile(gzFileName);
        System.out.println("Parsing IMDb dataset");
        return parseMoviesTsvFile(imdbExtraction.getFilePath(tsvFileName));
    }

    @Transactional(readOnly = true)
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public void importMovies(List<Movie> movies) {
        saveMovies(movies);
    }

    protected void saveMovies(List<Movie> movies) {
        System.out.println("Saving movies to database");
        int batchSize = 10000;

        for (int i = 0; i < movies.size(); i += batchSize) {
            int end = Math.min(i + batchSize, movies.size());
            List<Movie> batch = movies.subList(i, end);

            transactionTemplate.executeWithoutResult(status -> {
                movieRepository.saveAll(batch);
                entityManager.flush();
                entityManager.clear();
            });

            System.out.println("Saved " + end + " / " + movies.size() + " movies...");
        }
        System.out.println("Finished importing movies");
    }

    // From TMDB API to get additional movie info (backdrop, poster, overview)
    public TmdbMovieInfo getTmdbAdditionalInfo(String movieIdImdb) {
        System.out.println("[TMDB] Token configured: " + (tmdbApiToken != null && !tmdbApiToken.isEmpty()));

        if (tmdbApiToken == null || tmdbApiToken.isEmpty()) {
            System.err.println("[TMDB] API token is not configured!");
            return null;
        }

        String url = "https://api.themoviedb.org/3/find/" + movieIdImdb + "?external_source=imdb_id&language=en-US";
        System.out.println("[TMDB] Calling API for: " + movieIdImdb);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("accept", "application/json").header("Authorization", "Bearer " + tmdbApiToken).GET().build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("[TMDB] Response status for " + movieIdImdb + ": " + response.statusCode());

            if (response.statusCode() != 200) {
                System.err.println("[TMDB] API error for " + movieIdImdb + ": " + response.body());
                return null;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode movieResults = rootNode.get("movie_results");

            if (movieResults != null && movieResults.isArray() && !movieResults.isEmpty()) {
                JsonNode movie = movieResults.get(0);

                String backdropPath = movie.has("backdrop_path") && !movie.get("backdrop_path").isNull() ? movie.get("backdrop_path").asText() : null;
                String posterPath = movie.has("poster_path") && !movie.get("poster_path").isNull() ? movie.get("poster_path").asText() : null;
                String overview = movie.has("overview") && !movie.get("overview").isNull() ? movie.get("overview").asText() : null;

                System.out.println("[TMDB] Found for " + movieIdImdb + " - backdrop: " + backdropPath + ", poster: " + posterPath);
                return new TmdbMovieInfo(backdropPath, posterPath, overview);
            } else {
                System.out.println("[TMDB] No movie_results for " + movieIdImdb);
            }
        } catch (Exception e) {
            System.err.println("[TMDB] Failed for " + movieIdImdb + ": " + e.getMessage());
        }

        return null;
    }

    @Transactional
    public void enrichMovieWithTmdbInfo(String idImdb) {
        Movie movie = movieRepository.findByIdImdb(idImdb);
        if (movie == null) {
            System.out.println("Movie with IMDb ID " + idImdb + " not found");
            return;
        }
        try {
            TmdbMovieInfo tmdbInfo = getTmdbAdditionalInfo(idImdb);

            if (tmdbInfo != null) {
                movie.setBackdropPath(tmdbInfo.getFullBackdropUrl());
                movie.setPosterPath(tmdbInfo.getFullPosterUrl());
                movie.setOverview(tmdbInfo.getOverview());
                System.out.println("Updated TMDB info for movie: " + movie.getTitle());
            } else {
                System.out.println("No TMDB info found for movie with IMDb ID " + idImdb);
            }
//
//            // Fallback: si pas d'image TMDB, essayer de scraper depuis IMDb
//            if (movie.getPosterPath() == null && movie.getBackdropPath() == null) {
//                System.out.println("[FALLBACK] Trying to scrape image from IMDb for: " + idImdb);
//                try {
//                    String imdbImage = getMovieImage(idImdb);
//                    if (imdbImage != null && !imdbImage.isEmpty()) {
//                        movie.setImage(imdbImage);
//                        System.out.println("[FALLBACK] Got IMDb image for: " + movie.getTitle());
//                    }
//                } catch (Exception e) {
//                    System.err.println("[FALLBACK] Failed to scrape IMDb image for " + idImdb + ": " + e.getMessage());
//                }
//            }
//
            movie.setTmdbInfoChecked(true);
            movieRepository.save(movie);
        } catch (Exception e) {
            System.err.println("Failed to import TMDB info for movie with IMDb ID " + idImdb + ": " + e.getMessage());
        }
    }
//
//    /**
//     * Scrape movie image from IMDb.
//     */
//    public String getMovieImage(String movieIdImdb) throws IOException {
//        String url = String.format(ImdbLocationsService.imdbLocationsUrl, movieIdImdb);
//        WebDriver driver = null;
//
//        try {
//            driver = ImdbLocationsService.createWebDriver();
//            driver.get(url);
//            String pageSource = driver.getPageSource();
//            Document doc = Jsoup.parse(pageSource);
//            return doc.select("img[class='ipc-image']").attr("src");
//        } catch (Exception e) {
//            System.err.println("Failed to scrape image for movie " + movieIdImdb + e.getMessage() + e);
//            throw new IOException("Failed to scrape image", e);
//        } finally {
//            if (driver != null) {
//                driver.quit();
//            }
//        }
//    }
}