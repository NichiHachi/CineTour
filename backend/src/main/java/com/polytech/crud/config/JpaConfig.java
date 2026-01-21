package com.polytech.crud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.polytech.crud.repository",
        "com.polytech.crud.neo4j.sync.repository"
}, transactionManagerRef = "jpaTransactionManager")
public class JpaConfig {

    /**
     * Bean TransactionManager pour JPA/MySQL
     * Marqué comme @Primary car c'est le gestionnaire de transactions par défaut
     */
    @Bean("jpaTransactionManager")
    @Primary
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
