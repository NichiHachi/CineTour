package com.polytech.crud.neo4j.repository;

import com.polytech.crud.neo4j.entity.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserNodeRepository extends Neo4jRepository<UserNode, Long> {

    Optional<UserNode> findByUsername(String username);

    Optional<UserNode> findByEmail(String email);

    @Query("MATCH (u:User)-[s:SEARCHED]->(m:Movie) WHERE u.username = $username " +
            "RETURN u, s, m ORDER BY s.searchTime DESC")
    Optional<UserNode> findByUsernameWithSearchHistory(@Param("username") String username);
}
