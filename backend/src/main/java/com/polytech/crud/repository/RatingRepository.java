package com.polytech.crud.repository;

import com.polytech.crud.entity.Rating;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Rating findByIdImdb(String idImdb);
}
