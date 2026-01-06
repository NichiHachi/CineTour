package com.polytech.crud.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Node("MovieSearchHistory")
public class MovieSearchHistory {
    @Id
    @GeneratedValue
    private Long id;

    @Property("movieTitle")
    private String movieTitle;

    @Property("searchTime")
    private LocalDateTime searchTime;

    @Property("idImdb")
    private String idImdb;

    @Relationship(type = "HAS_SEARCH_HISTORY", direction = Relationship.Direction.INCOMING)
    private User user;
}
