package com.polytech.crud.neo4j.config;

import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!import")
@EnableNeo4jRepositories(basePackages = "com.polytech.crud.neo4j.repository", transactionManagerRef = "neo4jTransactionManager")
@EnableTransactionManagement
public class Neo4jConfig {

    /**
     * Bean TransactionManager pour Neo4j
     * Note: JPA aura son propre transactionManager (créé automatiquement)
     */
    @Bean("neo4jTransactionManager")
    public PlatformTransactionManager neo4jTransactionManager(
            Driver driver,
            DatabaseSelectionProvider databaseNameProvider) {
        return new Neo4jTransactionManager(driver, databaseNameProvider);
    }

    /**
     * Bean TransactionTemplate pour Neo4j
     * Permet la gestion programmatique des transactions Neo4j
     */
    @Bean("neo4jTransactionTemplate")
    public TransactionTemplate neo4jTransactionTemplate(
            @Qualifier("neo4jTransactionManager") PlatformTransactionManager neo4jTransactionManager) {
        return new TransactionTemplate(neo4jTransactionManager);
    }
}
