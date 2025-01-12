package com.polytech.crud.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.polytech.crud.entity.Location;
import com.polytech.crud.entity.Movie;
import com.polytech.crud.service.ImdbLocationsService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(ImdbLocationsService.class);

    @Autowired
    private ImdbLocationsService imdbLocationsService;

    /**
     * Method GET with a path variable.
     * Get locations by IMDB movie ID from the database.
     * 
     * @param idImdb String
     * @return
     */
    @GetMapping("/locationByImdbId/{idImdb}")
    public List<Location> findLocationByImdbId(@PathVariable String idImdb) {
        logger.info("Searching locations for IMDB ID: {}", idImdb);

        List<Location> locations = imdbLocationsService.getLocationsByImdbId(idImdb);
        if (locations.isEmpty()) {
            logger.info("No locations found for IMDB ID: {}", idImdb);
        }
        return locations;
    }

    /**
     * Method GET with a path variable.
     * Get locations by movie ID from the database.
     * 
     * @param id Long
     * @return
     */
    @GetMapping("/locationById/{id}")
    public List<Location> findLocationById(@PathVariable Long id) {
        logger.info("Searching locations for  ID: {}", id);

        List<Location> locations = imdbLocationsService.getLocationsById(id);
        if (locations.isEmpty()) {
            logger.info("No locations found for ID: {}", id);
        }
        return locations;
    }

    /**
     * Method GET with a path variable.
     * Get locations by title from the database, if not found, import locations from
     * IMDB if they exist.
     * 
     * @param title String
     * @return
     */
    @GetMapping("/location/{title}")
    public List<Location> findMoviesByLocations(@PathVariable String title) {
        logger.info("Searching locations for title: {}", title);

        List<Location> allLocations = new ArrayList<>();

        List<Movie> movies = imdbLocationsService.getMovieByTitle(title);
        if (movies.isEmpty()) {
            logger.info("Movie(s) not found in database: {}", title);
            return allLocations;
        }

        for (Movie movie : movies) {
            List<Location> locations = imdbLocationsService.getLocationsByImdbId(movie.getIdImdb());
            if (locations.isEmpty()) {
                try {
                    imdbLocationsService.importLocations(movie.getIdImdb());
                    locations = imdbLocationsService.getLocationsByImdbId(movie.getIdImdb());
                } catch (Exception e) {
                    logger.error("Error importing locations for movie {}: {}", movie.getIdImdb(), e);
                }
            }
            allLocations.addAll(locations);
        }

        if (allLocations.isEmpty()) {
            logger.info("No locations found for title: {}", title);
        }
        return allLocations;
    }

    /**
     * Method GET.
     * Get all locations from the database.
     * 
     * @return
     */
    @GetMapping("/locations")
    public List<Location> findAllLocations() {
        List<Location> locations = imdbLocationsService.getAllLocations();
        if (locations.isEmpty()) {
            logger.info("No locations found in database");
        }
        return locations;
    }
}
