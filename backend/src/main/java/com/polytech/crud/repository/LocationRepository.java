package com.polytech.crud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.polytech.crud.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByIdImdb(String idImdb);
}