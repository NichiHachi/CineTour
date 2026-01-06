package com.polytech.crud.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Node("Movie")
public class Movie {
    @Id
    @GeneratedValue
    private Long id;

    @Property("idImdb")
    private String idImdb;

    @Property("title")
    private String title;

    @Property("releaseYear")
    private Integer releaseYear;

    @Property("runtimeMinutes")
    private Integer runtimeMinutes;

    @Property("genres")
    private String genres;

    @Property("locationsChecked")
    private Boolean locationsChecked = false;

    @Property("locationSearchCount")
    private Integer locationSearchCount = 0;

    @Property("movieSearchCount")
    private Integer movieSearchCount = 0;

    @Property("image")
    private String image = "";
}
