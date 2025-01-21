package com.polytech.crud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String idImdb;
    @Column(length = 1000)
    private String title;
    @Column(nullable = true)
    private Integer releaseYear;
    @Column(nullable = true)
    private Integer runtimeMinutes;
    @Column(nullable = true)
    private String genres;
    private Boolean locationsChecked = false;
    private int locationSearchCount = 0;
    private int movieSearchCount = 0;
    private String image = "";
}