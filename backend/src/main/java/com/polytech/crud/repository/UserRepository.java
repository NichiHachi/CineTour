package com.polytech.crud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.polytech.crud.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}