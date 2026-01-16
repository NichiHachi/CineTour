package com.polytech.crud.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.polytech.crud.entity.Rating;
import com.polytech.crud.repository.RatingRepository;
import com.polytech.utils.ImdbDatasets;

import jakarta.persistence.EntityManager;

@Service
public class ImdbRatingsService {
    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ImdbExtraction imdbExtraction;

    private List<Rating> parseRatingsTsvFile(String tsvFilePath) throws IOException {
        List<Rating> ratings = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(tsvFilePath))) {
            // Skip header
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                try {
                    String[] fields = line.split("\t");
                    Rating rating = new Rating();
                    rating.setIdImdb(fields[0]);
                    if (!fields[1].equals("\\N")) {
                        rating.setAverageRating(Double.valueOf(fields[1]));
                    } else {
                        rating.setAverageRating(null);
                    }
                    if (!fields[2].equals("\\N")) {
                        rating.setNumVotes(Integer.valueOf(fields[2]));
                    } else {
                        rating.setNumVotes(Integer.valueOf(fields[2]));
                    }
                    ratings.add(rating);

                } catch (Exception e) {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
        }
        return ratings;
    }

    public List<Rating> getRatings() throws IOException {
        String gzFileName = ImdbDatasets.RATINGS.getFileName();
        String tsvFileName = gzFileName.replace(".gz", "");

        try {
            imdbExtraction.downloadFile(ImdbDatasets.RATINGS.getUrl(), gzFileName);
        } catch (Exception e) {
            System.out.println("Failed to download file: " + e.getMessage());
            return new ArrayList<>();
        }
        System.out.println("Extracting IMDb dataset");
        imdbExtraction.extractGzFile(gzFileName);
        System.out.println("Parsing IMDb dataset");
        return parseRatingsTsvFile(imdbExtraction.getFilePath(tsvFileName));
    }

    public void importRatings(List<Rating> ratings) {
        System.out.println("Saving ratings to database");

        int batchSize = 10000;

        for (int i = 0; i < ratings.size(); i += batchSize) {
            int end = Math.min(i + batchSize, ratings.size());
            List<Rating> batch = ratings.subList(i, end);

            transactionTemplate.executeWithoutResult(status -> {
                ratingRepository.saveAll(batch);
                entityManager.flush();
                entityManager.clear();
            });

            System.out.println("Saved " + end + " / " + ratings.size() + " ratings...");
        }

        System.out.println("Finished importing ratings");
    }

    public Rating getRatingByImdbId(String idImdb) {
        Rating rating = ratingRepository.findByIdImdb(idImdb);
        if (rating != null) {
            return rating;
        } else {
            return null;
        }
    }
}
