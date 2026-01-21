package com.polytech.crud.neo4j.entity;

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
@Node("Rating")
public class RatingNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("id_imdb")
    private String idImdb;

    @Property("average_rating")
    private Double averageRating;

    @Property("num_votes")
    private Integer numVotes;
}
