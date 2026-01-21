package com.polytech.crud.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.polytech.crud.entity.Principal;
import com.polytech.crud.repository.PrincipalRepository;
import com.polytech.utils.ImdbDatasets;

import jakarta.persistence.EntityManager;

@Service
public class ImdbPrincipalsService {
    @Autowired
    private PrincipalRepository principalRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ImdbExtraction imdbExtraction;

    private List<Principal> parsePrincipalsTsvFile(String tsvFilePath) throws IOException {
        List<Principal> principals = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(tsvFilePath))) {
            // Skip header
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                try {
                    String[] fields = line.split("\t");
                    Principal principal = new Principal();
                    principal.setIdImdb(fields[0]);
                    if (!fields[1].equals("\\N")) {
                        principal.setOrdering(Integer.valueOf(fields[1]));
                    } else {
                        principal.setOrdering(null);
                    }
                    if (!fields[2].equals("\\N")) {
                        principal.setNconst(fields[2]);
                    } else {
                        principal.setNconst(fields[2]);
                    }
                    if (!fields[2].equals("\\N")) {
                        principal.setCategory(fields[3]);
                    } else {
                        principal.setCategory(null);
                    }
                    if (!fields[4].equals("\\N")) {
                        principal.setJob(fields[4]);
                    } else {
                        principal.setJob(null);
                    }
                    if (!fields[5].equals("\\N")) {
                        principal.setCharacters(fields[5]);
                    } else {
                        principal.setCharacters(null);
                    }
                    principals.add(principal);

                } catch (Exception e) {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
        }
        return principals;
    }

    public List<Principal> getPrincipals() throws IOException {
        String gzFileName = ImdbDatasets.PRINCIPALS.getFileName();
        String tsvFileName = gzFileName.replace(".gz", "");

        try {
            imdbExtraction.downloadFile(ImdbDatasets.PRINCIPALS.getUrl(), gzFileName);
        } catch (Exception e) {
            System.out.println("Failed to download file: " + e.getMessage());
            return new ArrayList<>();
        }
        System.out.println("Extracting IMDb dataset");
        imdbExtraction.extractGzFile(gzFileName);
        System.out.println("Parsing IMDb dataset");
        return parsePrincipalsTsvFile(imdbExtraction.getFilePath(tsvFileName));
    }

    public void importPrincipals(List<Principal> principals) {
        System.out.println("Saving principals to database");

        int batchSize = 10000;

        for (int i = 0; i < principals.size(); i += batchSize) {
            int end = Math.min(i + batchSize, principals.size());
            List<Principal> batch = principals.subList(i, end);

            transactionTemplate.executeWithoutResult(status -> {
                principalRepository.saveAll(batch);
                entityManager.flush();
                entityManager.clear();
            });

            System.out.println("Saved " + end + " / " + principals.size() + " principals...");
        }

        System.out.println("Finished importing principals");
    }

    @Transactional(readOnly = true)
    public List<Principal> getPrincipalsByImdbId(String idImdb) {
        return principalRepository.findByIdImdb(idImdb);
    }

    public void importPrincipalsStreamingFromDataset(int batchSize) throws IOException {
        String gzFileName = ImdbDatasets.PRINCIPALS.getFileName();
        String tsvFileName = gzFileName.replace(".gz", "");

        try {
            imdbExtraction.downloadFile(ImdbDatasets.PRINCIPALS.getUrl(), gzFileName);
        } catch (Exception e) {
            System.out.println("Failed to download file: " + e.getMessage());
            return;
        }

        System.out.println("Extracting IMDb dataset");
        imdbExtraction.extractGzFile(gzFileName);
        System.out.println("Streaming parse and import of IMDb Principals dataset");

        List<Principal> batch = new ArrayList<>(batchSize);
        long totalSaved = 0L;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(imdbExtraction.getFilePath(tsvFileName)))) {
            // Skip header
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                try {
                    String[] fields = line.split("\t", -1);
                    Principal principal = new Principal();

                    if (fields.length > 0) principal.setIdImdb(fields[0]);
                    if (fields.length > 1 && !fields[1].equals("\\N")) {
                        try { principal.setOrdering(Integer.valueOf(fields[1])); } catch (Exception e) {}
                    }
                    if (fields.length > 2) principal.setNconst(fields[2].equals("\\N") ? null : fields[2]);
                    if (fields.length > 3) principal.setCategory(fields[3].equals("\\N") ? null : fields[3]);
                    if (fields.length > 4) principal.setJob(fields[4].equals("\\N") ? null : fields[4]);
                    if (fields.length > 5) principal.setCharacters(fields[5].equals("\\N") ? null : fields[5]);

                    batch.add(principal);
                } catch (Exception e) {
                    System.err.println("Skipping malformed line: " + line);
                }

                if (batch.size() >= batchSize) {
                    persistBatchPrincipals(batch);
                    totalSaved += batch.size();
                    System.out.println("Saved " + totalSaved + " Principals...");
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                persistBatchPrincipals(batch);
                totalSaved += batch.size();
                System.out.println("Saved " + totalSaved + " Principals (final)...");
            }
        }
    }

    private void persistBatchPrincipals(List<Principal> batch) {
        transactionTemplate.executeWithoutResult(status -> {
            principalRepository.saveAll(batch);
            entityManager.flush();
            entityManager.clear();
        });
    }

}
