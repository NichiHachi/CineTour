package com.polytech.crud.neo4j.entity;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Node("Movie")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("id_imdb")
    private String idImdb;

    @Property("title")
    private String title;

    @Property("release_year")
    private String releaseYear;

    @Property("runtime_minutes")
    private String runtimeMinutes;

    @Property("genres")
    private String genres;

    @Property("poster_path")
    private String posterPath;

    @Property("backdrop_path")
    private String backdropPath;

    @Property("overview")
    private String overview;

    @Property("locations_checked")
    private Boolean locationsChecked = false;

    @Property("location_search_count")
    private String locationSearchCount;

    @Property("movie_search_count")
    private String movieSearchCount;

    @Relationship(type = "FILMED_AT", direction = Relationship.Direction.OUTGOING)
    private Set<LocationNode> locations = new HashSet<>();

    @Relationship(type = "DIRECTED", direction = Relationship.Direction.INCOMING)
    private Set<PersonNode> directors = new HashSet<>();

    @Relationship(type = "HAS_PRINCIPAL", direction = Relationship.Direction.OUTGOING)
    private Set<PrincipalRelationship> principals = new HashSet<>();

    // Les ratings seront ajoutés plus tard comme propriétés du film
    @Property("average_rating")
    private Double averageRating;

    @Property("num_votes")
    private String numVotes;
}
