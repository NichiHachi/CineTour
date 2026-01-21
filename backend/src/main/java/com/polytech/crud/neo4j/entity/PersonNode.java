package com.polytech.crud.neo4j.entity;

import java.util.HashSet;
import java.util.Set;

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
@Node("Person")
public class PersonNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("nconst")
    private String nconst;

    @Property("primary_name")
    private String primaryName;

    @Property("birth_year")
    private String birthYear;

    @Property("death_year")
    private String deathYear;

    @Property("primary_professions")
    private Set<String> primaryProfessions = new HashSet<>();

    @Relationship(type = "KNOWN_FOR", direction = Relationship.Direction.OUTGOING)
    private Set<MovieNode> knownForMovies = new HashSet<>();
}
