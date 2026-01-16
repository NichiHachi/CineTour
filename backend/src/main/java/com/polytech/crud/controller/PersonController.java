package com.polytech.crud.controller;

import com.polytech.crud.entity.Person;
import com.polytech.crud.service.ImdbPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
public class PersonController {

    private static final Logger logger = Logger.getLogger(RatingController.class.getName());

    @Autowired
    private ImdbPersonService imdbPersonService;

    @GetMapping("/personByNconst/{nconst}")
    public Person getPersonByNconst(@PathVariable String nconst) {
        logger.info("Fetching person for Nconst: " + nconst);
        Person person = imdbPersonService.getPersonByNconst(nconst);
        if (person == null) {
            logger.warning("No person found for Nconst: " + nconst);
        }
        return person;
    }
}
