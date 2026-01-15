package com.polytech.crud.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.polytech.crud.entity.Person;
import com.polytech.crud.repository.PersonRepository;
import com.polytech.utils.ImdbDatasets;

import jakarta.persistence.EntityManager;

@Service
public class ImdbPersonService {
    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private List<Person> parsePersonsTsvFile(String tsvFilePath) throws IOException {
        List<Person> Persons = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(tsvFilePath))) {
            // Skip header
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                try {
                    String[] fields = line.split("\t");
                    Person Person = new Person();
                    Person.setNconst(fields[0]);
                    Person.setPrimaryName(fields[1]);
                    if (!fields[2].equals("\\N")) {
                        Person.setBirthYear(Year.of(Integer.parseInt(fields[2])));
                    } else {
                        Person.setBirthYear(null);
                    }
                    if (!fields[3].equals("\\N")) {
                        Person.setDeathYear(Year.of(Integer.parseInt(fields[3])));
                    } else {
                        Person.setDeathYear(null);
                    }
                    if (!fields[4].equals("\\N")) {
                        Person.setPrimaryProfessions(Arrays.asList(fields[4].split(",")));
                    } else {
                        Person.setPrimaryProfessions(new ArrayList<>());
                    }
                    if (!fields[5].equals("\\N")) {
                        Person.setKnownForTitles(Arrays.asList(fields[5].split(",")));
                    } else {
                        Person.setKnownForTitles(new ArrayList<>());
                    }
                    Persons.add(Person);

                } catch (Exception e) {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
        }
        return Persons;
    }

    public List<Person> getPersons() throws IOException {
        String gzFileName = ImdbDatasets.NAMES.getFileName();
        String tsvFileName = gzFileName.replace(".gz", "");

        try {
            ImdbExtraction.downloadFile(ImdbDatasets.NAMES.getUrl(), gzFileName);
        } catch (Exception e) {
            System.out.println("Failed to download file: " + e.getMessage());
            return new ArrayList<>();
        }
        System.out.println("Extracting IMDb dataset");
        ImdbExtraction.extractGzFile(gzFileName);
        System.out.println("Parsing IMDb dataset");
        return parsePersonsTsvFile(tsvFileName);
    }

    @Transactional(readOnly = true)
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    public void importPersons(List<Person> Persons) {
        System.out.println("Saving Persons to database");

        int batchSize = 10000;

        for (int i = 0; i < Persons.size(); i += batchSize) {
            int end = Math.min(i + batchSize, Persons.size());
            List<Person> batch = Persons.subList(i, end);

            transactionTemplate.executeWithoutResult(status -> {
                personRepository.saveAll(batch);
                entityManager.flush();
                entityManager.clear();
            });

            System.out.println("Saved " + end + " / " + Persons.size() + " Persons...");
        }

        System.out.println("Finished importing Persons");
    }

    @Transactional(readOnly = true)
    public Person getPersonByNconst(String nconst) {
        return personRepository.findByNconst(nconst);
    }
}
