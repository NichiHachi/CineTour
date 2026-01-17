package com.polytech.crud.neo4j.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Node("Rating")
public class RatingNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("idImdb")
    private String idImdb;

    @Property("averageRating")
    private Double averageRating;

    @Property("numVotes")
    private Integer numVotes;
}
