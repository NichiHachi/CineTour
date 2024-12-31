package com.polytech.crud.service;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.polytech.crud.entity.Movie;
import com.polytech.crud.repository.MovieRepository;

@Service
public class ImdbMoviesService {
    private final String imdbTitleBasicsUrl = "https://datasets.imdbws.com/title.basics.tsv.gz";

    @Autowired
    private MovieRepository movieRepository;

    public void downloadFile(String filePath) throws Exception {
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

    public void extractGzFile(String gzFilePath, String tsvFilePath) throws IOException {
        try (GZIPInputStream gzipIn = new GZIPInputStream(new FileInputStream(gzFilePath));
                OutputStream out = new FileOutputStream(tsvFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    public List<Movie> parseTsvFile(String tsvFilePath) throws IOException {
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
                        movies.add(movie);
                    }
                } catch (Exception e) {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
        }
        return movies;
    }

    public void importMovies(String gzFilePath, String tsvFilePath) throws IOException {
        // File operations without transaction
        try {
            downloadFile(gzFilePath);
        } catch (Exception e) {
            System.out.println("Failed to download file: " + e.getMessage());
            return;
        }
        System.out.println("Extracting IMDb dataset");
        extractGzFile(gzFilePath, tsvFilePath);
        System.out.println("Parsing IMDb dataset");
        List<Movie> movies = parseTsvFile(tsvFilePath);

        // Database operation with transaction
        saveMovies(movies);
    }

    @Transactional
    protected void saveMovies(List<Movie> movies) {
        System.out.println("Saving movies to database");
        movieRepository.saveAll(movies);
        System.out.println("Finished importing movies");
    }
}