package com.polytech.crud.neo4j.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RelationshipProperties
public class MovieSearchRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @Property("movieTitle")
    private String movieTitle;

    @Property("searchTime")
    private LocalDateTime searchTime;

    @Property("idImdb")
    private String idImdb;

    @TargetNode
    private MovieNode movie;
}
