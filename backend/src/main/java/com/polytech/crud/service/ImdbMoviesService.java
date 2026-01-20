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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

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

    // From TMDB API to get movie images
    public String getMovieImage(String movieIdImdb) throws IOException {
        if (tmdbApiToken == null || tmdbApiToken.isEmpty()) {
            System.err.println("TMDB API token is not configured");
            return null;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.themoviedb.org/3/find/" + movieIdImdb + "?external_source=imdb_id&language=en-US"))
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + tmdbApiToken)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            // TODO: Parse JSON response to extract image URL
            return response.body();
        } catch (Exception e) {
            System.err.println("Failed to fetch movie image from TMDB API: " + e.getMessage());
        }

        return null;
    }

    public void importMovieImage(String idImdb) {
        Movie movie = movieRepository.findByIdImdb(idImdb);
        if (movie == null) {
            System.out.println("Movie with IMDb ID " + idImdb + " not found");
            return;
        }
        try {
            ImdbLocationsService imdbLocationsService = new ImdbLocationsService();
            String image = getMovieImage(idImdb);

            // TODO

            movieRepository.save(movie);
        } catch (Exception e) {
            System.err.println("Failed to import image for movie with IMDb ID " + idImdb + ": " + e.getMessage());
        }
    }
}