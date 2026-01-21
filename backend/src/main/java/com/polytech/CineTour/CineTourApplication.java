package com.polytech.CineTour;

import java.util.List;

import com.polytech.crud.entity.*;
import com.polytech.crud.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.polytech.utils.Console;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableAsync
@EntityScan(basePackages = { "com.polytech.crud.entity", "com.polytech.crud.neo4j.entity" })
@EnableJpaRepositories(basePackages = "com.polytech.crud.repository")
@ComponentScan(basePackages = {
        "com.polytech.crud.controller",
        "com.polytech.crud.service",
        "com.polytech.crud.neo4j"
})
@RestController
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class CineTourApplication implements CommandLineRunner {

    @Autowired
    private ImdbMoviesService imdbMovies;

    @Autowired
    private ImdbDirectorsService imdbDirectors;

    @Autowired
    private ImdbPersonService imdbPersons;

    @Autowired
    private ImdbLocationsService imdbLocations;

    @Autowired
    private ImdbPrincipalsService imdbPrincipals;

    @Autowired
    private ImdbRatingsService imdbRatings;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @PostConstruct
    public void init() {
        Console.warnln("Active profile: " + activeProfile + "\n");
    }

    public static void main(String[] args) {
        SpringApplication.run(CineTourApplication.class, args);
    }

    /**
     * Run the application.
     * If the active profile is “import”, then first import the films and then
     * switch to the default state.
     * Otherwise, the default profile is “default”.
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        if ("import".equals(activeProfile)) {
            Console.warnln("Import mode activated\n");

            // Movies
            Console.warnln("Movies importation started\n");
            List<Movie> movies = imdbMovies.getMovies();
            imdbMovies.importMovies(movies);
            Console.warnln(movies.size() + " Movies imported\n");

            // Ratings
            Console.warnln("Ratings importation started");
            List<Rating> ratings = imdbRatings.getRatings();
            imdbRatings.importRatings(ratings);
            Console.warnln(ratings.size() + " Ratings imported\n");

            // Import locations for a SHAWSHANK REDEMPTION (Les évadés)
            imdbLocations.importLocations("tt0111161");

            // Directors
            Console.warnln("Directors importation started\n");
            List<Director> directors = imdbDirectors.getDirectors();
            imdbDirectors.importDirectors(directors);
            Console.warnln(directors.size() + " Directors imported\n");

            // Persons : Actors + Directors informations
            Console.warnln("Persons importation started\n");
            //List<Person> persons = imdbPersons.getPersons();
            //imdbPersons.importPersons(persons);
            //Console.warnln(persons.size() + " Persons imported\n");
            int batchSize = 10000; // Adjust as needed for your memory constraints
            imdbPersons.importPersonsStreamingFromDataset(batchSize);
            Console.warnln("Persons importation finished\n");

            // Principals
            Console.warnln("Principals importation started");
            int batchSizePrincipals = 1000;
            imdbPrincipals.importPrincipalsStreamingFromDataset(batchSizePrincipals);
            Console.warnln("Principals importation finished\n");

            System.exit(0); // Clean exit after import
        } else {
            Console.warnln("Default mode activated\n");

            // Import locations for a SHAWSHANK REDEMPTION (Les évadés) to ensure correct
            // operation when duplicating the same import
            imdbLocations.importLocations("tt0111161");
        }
    }

    @GetMapping
    public String hello() {
        return "Hello World";
    }
}