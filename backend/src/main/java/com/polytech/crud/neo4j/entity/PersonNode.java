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
@Node("Person")
public class PersonNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("nconst")
    private String nconst;

    @Property("primaryName")
    private String primaryName;

    @Property("birthYear")
    private Integer birthYear;

    @Property("deathYear")
    private Integer deathYear;

    @Property("primaryProfessions")
    private Set<String> primaryProfessions = new HashSet<>();

    @Relationship(type = "KNOWN_FOR", direction = Relationship.Direction.OUTGOING)
    private Set<MovieNode> knownForMovies = new HashSet<>();
}
