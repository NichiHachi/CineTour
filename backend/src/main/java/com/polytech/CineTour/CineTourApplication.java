package com.polytech.CineTour;

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

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @PostConstruct
    public void init() {
        Console.warnln("Active profile: " + activeProfile + "\n");
    }

    public static void main(String[] args) {
        SpringApplication.run(CineTourApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if ("import".equals(activeProfile)) {
            Console.warnln("Import mode activated\n");
            String gzFilePath = "title.basics.tsv.gz";
            String tsvFilePath = "title.basics.tsv";
            imdbMovies.importMovies(gzFilePath, tsvFilePath);
            Console.warnln("Import finished, restart application with default profile if needed\n");
            System.exit(0); // Clean exit after import
        } else {
            Console.warnln("Default mode activated\n");
        }
    }

    @GetMapping
    public String hello() {
        return "Hello World";
    }
}