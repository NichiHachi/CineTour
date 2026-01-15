package com.polytech.crud.controller;

import com.polytech.crud.entity.Rating;
import com.polytech.crud.service.ImdbRatingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
public class RatingController {

    private static final Logger logger = Logger.getLogger(RatingController.class.getName());

    @Autowired
    private ImdbRatingsService imdbRatingsService;

    @GetMapping("/ratingByImdbId/{idImdb}")
    public Rating getRatingByImdbId(@PathVariable String idImdb) {
        logger.info("Fetching rating for IMDB ID: " + idImdb);
        Rating rating = imdbRatingsService.getRatingByImdbId(idImdb);
        if (rating == null) {
            logger.warning("No rating found for IMDB ID: " + idImdb);
        }
        return rating;
    }
}
