package com.polytech.crud.repository;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import com.polytech.crud.entity.Location;

public interface LocationRepository extends Neo4jRepository<Location, Long> {
    List<Location> findByIdImdb(String idImdb);
}
