package com.polytech.crud.repository;

import com.polytech.crud.entity.MovieSearchHistory;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieSearchHistoryRepository extends Neo4jRepository<MovieSearchHistory, Long> {
    @Query("MATCH (u:User)-[:HAS_SEARCH_HISTORY]->(msh:MovieSearchHistory) WHERE id(u) = $userId RETURN msh")
    List<MovieSearchHistory> findByUserId(@Param("userId") Long userId);

    MovieSearchHistory findByIdImdb(String imdbId);
}
