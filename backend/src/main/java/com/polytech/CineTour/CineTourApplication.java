package com.polytech.CineTour;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class CineTourApplication {

	public static void main(String[] args) {
		SpringApplication.run(CineTourApplication.class, args);
	}

	@GetMapping
	public String hello(){
		return "Hello World";
	}

}