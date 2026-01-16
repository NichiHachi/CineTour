package com.polytech.crud.repository;

import com.polytech.crud.entity.Director;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectorRepository extends JpaRepository<Director, Long> {
    Director findByIdImdb (String idImdb);
}
