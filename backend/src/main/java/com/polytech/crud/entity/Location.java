package com.polytech.crud.entity;

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
@Node("Location")
public class Location {
    @Id
    @GeneratedValue
    private Long id;

    @Property("idImdb")
    private String idImdb;

    @Property("name")
    private String name;

    @Property("locationString")
    private String locationString;

    @Property("description")
    private String description;
    private Double latitude;
    private Double longitude;
    private String displayName;
    private String countryCode;
    private Boolean geocodingFailed = false; 
}