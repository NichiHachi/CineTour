package com.polytech.crud.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polytech.crud.entity.Movie;
import com.polytech.crud.repository.MovieRepository;

@Service
public class ImdbMoviesService {
    private final String imdbTitleBasicsUrl = "https://datasets.imdbws.com/title.basics.tsv.gz";

    @Autowired
    private MovieRepository movieRepository;

    private void downloadFile(String filePath) throws Exception {
        System.out.println("Downloading file from " + imdbTitleBasicsUrl + " to " + filePath);
        URL url = new URI(imdbTitleBasicsUrl).toURL();
        try (InputStream in = url.openStream();
                OutputStream out = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private void extractGzFile(String gzFilePath, String tsvFilePath) throws IOException {
        try (GZIPInputStream gzipIn = new GZIPInputStream(new FileInputStream(gzFilePath));
                OutputStream out = new FileOutputStream(tsvFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private List<Movie> parseTsvFile(String tsvFilePath) throws IOException {
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

    public List<Movie> getMovies(String gzFilePath, String tsvFilePath) throws IOException {
        try {
            downloadFile(gzFilePath);
        } catch (Exception e) {
            System.out.println("Failed to download file: " + e.getMessage());
            return new ArrayList<>();
        }
        System.out.println("Extracting IMDb dataset");
        extractGzFile(gzFilePath, tsvFilePath);
        System.out.println("Parsing IMDb dataset");
        return parseTsvFile(tsvFilePath);
    }

    @Transactional(readOnly = true)
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public void importMovies(List<Movie> movies) {
        saveMovies(movies);
    }

    @Transactional
    protected void saveMovies(List<Movie> movies) {
        System.out.println("Saving movies to database");
        movieRepository.saveAll(movies);
        System.out.println("Finished importing movies");
    }

    public void importMovieImage(String idImdb) {
        Movie movie = movieRepository.findByIdImdb(idImdb);
        if (movie == null) {
            System.out.println("Movie with IMDb ID " + idImdb + " not found");
            return;
        }
        try {
            ImdbLocationsService imdbLocationsService = new ImdbLocationsService();
            String image = imdbLocationsService.getMovieImage(idImdb);
            movie.setImage(image);
            movieRepository.save(movie);
        } catch (Exception e) {
            System.err.println("Failed to import image for movie with IMDb ID " + idImdb + ": " + e.getMessage());
        }
    }
}