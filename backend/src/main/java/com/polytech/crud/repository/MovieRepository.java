package com.polytech.crud.repository;

import java.time.Year;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.polytech.crud.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findByTitle(String name);

    Movie findByIdImdb(String idImdb);

    @Query("SELECT m FROM Movie m " +
            "LEFT JOIN Rating r ON m.idImdb = r.idImdb " +
            "WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
            "AND (:fromYear IS NULL OR m.releaseYear >= :fromYear) " +
            "AND (:toYear IS NULL OR m.releaseYear <= :toYear) " +
            "AND (:genre1 IS NULL OR LOWER(m.genres) LIKE LOWER(CONCAT('%', :genre1, '%'))) " +
            "AND (:genre2 IS NULL OR LOWER(m.genres) LIKE LOWER(CONCAT('%', :genre2, '%'))) " +
            "AND (:genre3 IS NULL OR LOWER(m.genres) LIKE LOWER(CONCAT('%', :genre3, '%'))) " +
            "AND (:minRating IS NULL OR (r.averageRating IS NOT NULL AND r.averageRating >= :minRating)) " +
            "AND (:maxRating IS NULL OR (r.averageRating IS NOT NULL AND r.averageRating <= :maxRating)) " +
            "ORDER BY (m.movieSearchCount + m.locationSearchCount) DESC")
    Page<Movie> searchMoviesWithFilters(
            @Param("title") String title,
            @Param("fromYear") Integer fromYear,
            @Param("toYear") Integer toYear,
            @Param("genre1") String genre1,
            @Param("genre2") String genre2,
            @Param("genre3") String genre3,
            @Param("minRating") Double minRating,
            @Param("maxRating") Double maxRating,
            Pageable pageable
    );

}
