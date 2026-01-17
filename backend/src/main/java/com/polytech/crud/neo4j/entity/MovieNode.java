package com.polytech.crud.neo4j.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Node("Movie")
public class MovieNode {

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

    @Property("image")
    private String image;

    @Property("locationsChecked")
    private Boolean locationsChecked = false;

    @Property("locationSearchCount")
    private Integer locationSearchCount = 0;

    @Property("movieSearchCount")
    private Integer movieSearchCount = 0;

    @Relationship(type = "FILMED_AT", direction = Relationship.Direction.OUTGOING)
    private Set<LocationNode> locations = new HashSet<>();

    @Relationship(type = "DIRECTED_BY", direction = Relationship.Direction.OUTGOING)
    private Set<PersonNode> directors = new HashSet<>();

    @Relationship(type = "HAS_PRINCIPAL", direction = Relationship.Direction.OUTGOING)
    private Set<PrincipalRelationship> principals = new HashSet<>();

    @Relationship(type = "HAS_RATING", direction = Relationship.Direction.OUTGOING)
    private RatingNode rating;
}
