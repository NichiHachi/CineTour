package com.polytech.crud.repository;

import com.polytech.crud.entity.Principal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PrincipalRepository extends JpaRepository<Principal, Long> {
    Principal findByIdImdbAndNconstAndOrdering(String idImdb, String nconst, Integer ordering);
}
