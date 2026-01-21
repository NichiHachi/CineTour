package com.polytech.crud.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "principals")
public class Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "principal_seq")
    @SequenceGenerator(name = "principal_seq", sequenceName = "principal_sequence", allocationSize = 50)
    private Long id;
    private String idImdb;
    private Integer ordering;
    private String nconst;
    private String category;
    private String job;
    private String characters;
}
