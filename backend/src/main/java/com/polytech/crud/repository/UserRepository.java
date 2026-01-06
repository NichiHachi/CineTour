package com.polytech.crud.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import com.polytech.crud.entity.User;

public interface UserRepository extends Neo4jRepository<User, Long> {
    User findByUsername(String username);
}
