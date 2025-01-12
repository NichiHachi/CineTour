package com.polytech.CineTour;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.polytech.crud.entity.Movie;
import com.polytech.crud.service.ImdbLocationsService;
import com.polytech.crud.service.ImdbMoviesService;
import com.polytech.utils.Console;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EntityScan(basePackages = "com.polytech.crud.entity")
@EnableJpaRepositories(basePackages = "com.polytech.crud.repository")
@ComponentScan(basePackages = {
        "com.polytech.crud.controller",
        "com.polytech.crud.service",
})
@RestController
public class CineTourApplication implements CommandLineRunner {

    @Autowired
    private ImdbMoviesService imdbMovies;

    @Autowired
    private ImdbLocationsService imdbLocations;

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
            String gzFilePath = "title.basics.tsv.gz";
            String tsvFilePath = "title.basics.tsv";
            List<Movie> movies = imdbMovies.getMovies(gzFilePath, tsvFilePath);
            imdbMovies.importMovies(movies);
            Console.warnln(movies.size() + " Movies imported\n");

            // Import locations for a SHAWSHANK REDEMPTION (Les évadés)
            imdbLocations.importLocations("tt0111161");

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