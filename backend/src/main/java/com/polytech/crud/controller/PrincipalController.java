package com.polytech.crud.controller;

import com.polytech.crud.entity.Principal;
import com.polytech.crud.service.ImdbPrincipalsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@RestController
public class PrincipalController {

    private static final Logger logger = Logger.getLogger(PrincipalController.class.getName());

    @Autowired
    private ImdbPrincipalsService imdbPrincipalsService;

    @GetMapping("/principalsByImdbId/{idImdb}")
    public List<Principal> getPrincipalsByImdbId(@PathVariable String idImdb) {
        logger.info("Getting principals for IMDB ID: " + idImdb);
        return imdbPrincipalsService.getPrincipalsByImdbId(idImdb);
    }
}
