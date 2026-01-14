package com.polytech.crud.repository;

import com.polytech.crud.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PersonRepository extends JpaRepository<Person, Long> {
    Person findByNconst (String Nconst);
}
