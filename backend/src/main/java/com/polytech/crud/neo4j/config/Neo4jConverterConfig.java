package com.polytech.crud.neo4j.config;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.neo4j.core.convert.Neo4jConversions;

@Configuration
public class Neo4jConverterConfig {

    /**
     * Convertisseur personnalisé pour gérer la conversion INTEGER -> String
     * depuis Neo4j
     */
    public static class Neo4jIntegerToStringConverter implements Converter<Value, String> {
        @Override
        public String convert(Value source) {
            if (source.isNull()) {
                return null;
            }

            // Si c'est déjà une String, la retourner
            if (source.hasType(TypeSystem.getDefault().STRING())) {
                return source.asString();
            }

            // Si c'est un Integer/Long, le convertir en String
            if (source.hasType(TypeSystem.getDefault().INTEGER())) {
                return String.valueOf(source.asLong());
            }

            // Sinon, tenter une conversion en String
            return source.toString();
        }
    }

    @Bean
    public Neo4jConversions neo4jConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new Neo4jIntegerToStringConverter());
        return new Neo4jConversions(converters);
    }
}
