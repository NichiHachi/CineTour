package com.polytech.crud.neo4j.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import org.springframework.context.annotation.Profile;

@Data
@Profile("!import")
@AllArgsConstructor
@NoArgsConstructor
@Node("Location")
public class LocationNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("idImdb")
    private String idImdb;

    @Property("locationString")
    private String locationString;

    @Property("description")
    private String description;

    @Property("latitude")
    private Double latitude;

    @Property("longitude")
    private Double longitude;

    @Property("displayName")
    private String displayName;

    @Property("countryCode")
    private String countryCode;

    @Property("geocodingFailed")
    private Boolean geocodingFailed = false;
}
