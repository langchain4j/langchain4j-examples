package com.example.demo;

import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * NOTE:
 * This example assumes we have a Neo4j instance with Bolt URI bolt://localhost:7687, username 'neo4j' and password 'pass1234'
 * as specified in the application.properties.
 * If needed, change the properties values to address a running instance.
 * 
 * To add an embedding
 * curl -X POST localhost:8083/api/embeddings/add -H "Content-Type: text/plain" -d "embeddingTest"
 * 
 * To search embeddings
 * curl -X POST localhost:8083/api/embeddings/search -H "Content-Type: text/plain" -d "querySearchTest"
 */
@SpringBootApplication
public class SpringBootExample {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootExample.class, args);
    }

    @Bean
    public AllMiniLmL6V2EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }
    
}
