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
@Node("User")
public class UserNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("username")
    private String username;

    @Property("password")
    private String password;

    @Property("email")
    private String email;

    @Relationship(type = "SEARCHED", direction = Relationship.Direction.OUTGOING)
    private Set<MovieSearchRelationship> searchHistory = new HashSet<>();
}
