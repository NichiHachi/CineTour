package com.polytech.crud.neo4j.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableNeo4jRepositories(basePackages = "com.polytech.crud.neo4j.repository")
@EnableTransactionManagement
public class Neo4jConfig {
    
    /**
     * Bean TransactionTemplate pour Neo4j
     * Permet la gestion programmatique des transactions Neo4j
     */
    @Bean
    public TransactionTemplate neo4jTransactionTemplate(
            @Qualifier("transactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
