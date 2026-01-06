package com.polytech.crud.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Node("User")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Property("username")
    private String username;

    @Property("password")
    private String password;

    @Property("email")
    private String email;

    @Relationship(type = "HAS_SEARCH_HISTORY", direction = Relationship.Direction.OUTGOING)
    private List<MovieSearchHistory> movieSearchHistory = new ArrayList<>();
}
