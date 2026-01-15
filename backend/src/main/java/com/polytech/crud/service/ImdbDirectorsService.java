package com.polytech.crud.service;

import com.polytech.crud.entity.Director;
import com.polytech.crud.repository.DirectorRepository;
import com.polytech.utils.ImdbDatasets;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ImdbDirectorsService {
    @Autowired
    private DirectorRepository directorRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private List<Director> parseDirectorsTsvFile(String tsvFilePath) throws IOException {
        List<Director> directors = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(tsvFilePath))) {
            // Skip header
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                try {
                    String[] fields = line.split("\t");
                    Director director = new Director();
                    director.setIdImdb(fields[0]);
                    if (!fields[1].equals("\\N")) {
                        director.setDirectors(Arrays.asList(fields[1].split(",")));
                    } else {
                        director.setDirectors(new ArrayList<>());
                    }
                    directors.add(director);

                } catch (Exception e) {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
        }
        return directors;
    }

    public List<Director> getDirectors() throws IOException {
        String gzFileName = ImdbDatasets.CREW.getFileName();
        String tsvFileName = gzFileName.replace(".gz", "");

        try {
            ImdbExtraction.downloadFile(ImdbDatasets.CREW.getUrl(), gzFileName);
        } catch (Exception e) {
            System.out.println("Failed to download file: " + e.getMessage());
            return new ArrayList<>();
        }
        System.out.println("Extracting IMDb dataset");
        ImdbExtraction.extractGzFile(gzFileName);
        System.out.println("Parsing IMDb dataset");
        return parseDirectorsTsvFile(tsvFileName);
    }

    public void importDirectors(List<Director> directors) {
        System.out.println("Saving directors to database");

        int batchSize = 10000;

        for (int i = 0; i < directors.size(); i += batchSize) {
            int end = Math.min(i + batchSize, directors.size());
            List<Director> batch = directors.subList(i, end);

            transactionTemplate.executeWithoutResult(status -> {
                directorRepository.saveAll(batch);
                entityManager.flush();
                entityManager.clear();
            });

            System.out.println("Saved " + end + " / " + directors.size() + " directors...");
        }

        System.out.println("Finished importing directors");
    }

    @Transactional(readOnly = true)
    public Director getDirectorsByImdbId(String idImdb) {
        return directorRepository.findByIdImdb(idImdb);
    }
}
