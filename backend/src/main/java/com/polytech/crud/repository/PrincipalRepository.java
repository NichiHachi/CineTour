package com.polytech.crud.repository;

import com.polytech.crud.entity.Principal;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PrincipalRepository extends JpaRepository<Principal, Long> {
    List<Principal> findByIdImdb(String idImdb);
    Principal findByIdImdbAndNconstAndOrdering(String idImdb, String nconst, Integer ordering);
}
