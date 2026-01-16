package com.polytech.crud.controller;

import com.polytech.crud.entity.Director;
import com.polytech.crud.service.ImdbDirectorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
public class DirectorController {

    private static final Logger logger = Logger.getLogger(DirectorController.class.getName());

    @Autowired
    private ImdbDirectorsService imdbDirectorsService;

    @GetMapping("/directorsByImdbId/{idImdb}")
    public Director getDirectorsByImdbId(@PathVariable String idImdb) {
        logger.info("Getting directors for IMDB ID: " + idImdb);
        return imdbDirectorsService.getDirectorsByImdbId(idImdb);
    }
}
