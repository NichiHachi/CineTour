package com.polytech.crud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.polytech.crud.repository",
        "com.polytech.crud.neo4j.sync.repository"
}, transactionManagerRef = "transactionManager")
@EnableTransactionManagement
public class JpaConfig {

    /**
     * Bean TransactionManager pour JPA/MySQL
     * Marqué comme @Primary car c'est le gestionnaire de transactions par défaut
     */
    @Bean("transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
