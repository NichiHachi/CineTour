package com.polytech.crud.controller;

import java.util.List;

import com.polytech.utils.Console;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.polytech.crud.entity.Location;
import com.polytech.crud.service.ImdbLocationsService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

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
    public List<Location> findLocationByImdbId(@PathVariable String idImdb) throws Exception {
        logger.info("Searching locations for IMDB ID: {}", idImdb);

        List<Location> locations = imdbLocationsService.getLocationsByImdbId(idImdb);

        boolean needImport = locations.isEmpty();
        if (!needImport) {
            for (Location loc : locations) {
                if (!Boolean.TRUE.equals(loc.getLocationChecked())) {
                    Console.warnln("Location data incomplete for IMDB ID: " + idImdb + ", re-importing locations.");
                    needImport = true;
                    break;
                }
            }
        }

        if (needImport) {
            imdbLocationsService.importLocations(idImdb);
            locations = imdbLocationsService.getLocationsByImdbId(idImdb);
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

    @GetMapping("/locations/migrate")
    public String migrateLocations() throws InterruptedException {
        imdbLocationsService.migrateExistingLocations();
        return "Migration triggered";
    }
}
