package com.polytech.crud.neo4j.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.context.annotation.Profile;

@Data
@Profile("!import")
@AllArgsConstructor
@NoArgsConstructor
@RelationshipProperties
public class PrincipalRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @Property("ordering")
    private String ordering;

    @Property("category")
    private String category;

    @Property("job")
    private String job;

    @Property("characters")
    private String characters;

    @TargetNode
    private PersonNode person;
}
