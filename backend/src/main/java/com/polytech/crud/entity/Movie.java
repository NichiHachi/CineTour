package com.polytech.crud.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

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

    @Property("imdb_id")
    private String imdbId;

    @Property("title")
    private String title;
    private Integer releaseYear;
    private Integer runtimeMinutes;
    private String genres;

    @Property("locationsChecked")
    private Boolean locationsChecked = false;

    @Property("locationSearchCount")
    private Integer locationSearchCount = 0;

    @Property("movieSearchCount")
    private Integer movieSearchCount = 0;

    @Property("image")
    private String image = "";

    // Relations
    @Relationship(type = "HAS_GENRE", direction = Relationship.Direction.OUTGOING)
    private List<Genre> genreList = new ArrayList<>();

    @Relationship(type = "ACTED_IN", direction = Relationship.Direction.INCOMING)
    private List<Person> actors = new ArrayList<>();

    @Relationship(type = "DIRECTED", direction = Relationship.Direction.INCOMING)
    private List<Person> directors = new ArrayList<>();

    @Relationship(type = "PRODUCED_BY", direction = Relationship.Direction.OUTGOING)
    private List<Producer> producers = new ArrayList<>();

    @Relationship(type = "FILM_LOCATION", direction = Relationship.Direction.OUTGOING)
    private List<Location> filmLocations = new ArrayList<>();

    @Relationship(type = "NARRATIVE_LOCATION", direction = Relationship.Direction.OUTGOING)
    private List<Location> narrativeLocations = new ArrayList<>();
}
