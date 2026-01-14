package com.polytech.crud.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Year;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "persons")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nconst;
    private String primaryName;
    private Year birthYear;
    private Year deathYear;
    @ElementCollection
    private List<String> primaryProfessions;
    @ElementCollection
    private List<String> knownForTitles; // idImdb of movies
}
