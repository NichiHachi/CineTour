package com.polytech.CineTour;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.polytech.db.DataConnection;

@SpringBootApplication
@EntityScan(basePackages = "com.polytech.crud.entity")
@EnableJpaRepositories(basePackages = "com.polytech.crud.repository")
@ComponentScan(basePackages = {"com.polytech.crud.controller", "com.polytech.crud.service"})
@RestController
public class CineTourApplication {

	public static void main(String[] args) {
		SpringApplication.run(CineTourApplication.class, args);

		DataConnection dataConnection = new DataConnection();
		dataConnection.connect();
		dataConnection.executeQuery("SHOW DATABASES;");
		dataConnection.executeQuery("SHOW TABLES;");
		dataConnection.executeQuery("SELECT * FROM movies;");
		dataConnection.disconnect();
	}

	@GetMapping
	public String hello(){
		return "Hello World";
	}

}