package it.dev.langchan4j.example;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

public class ModelResourceIT {

    private final String  baseUrl = "http://localhost:9080/api/model/";
    
    private Client client;

    @BeforeEach
    public void setup() {
        client = ClientBuilder.newClient();
    }

    @AfterEach
    public void teardown() {
      client.close();
    }
    
    @Test
    public void testLanguageMode() {
        String url = baseUrl + "language?question=When was langchain4j launched?";
        Response response = client.target(url).request().get();
        String answer = response.readEntity(String.class);
        assertTrue(answer.contains("2017"), "actual: " + answer);
    }

    @Test
    public void testChatMode() {
        String url = baseUrl + "chat?userMessage=Which are the most used Large Language Models?";
        Response response = client.target(url).request().get();
        String answer = response.readEntity(String.class);
        assertTrue(answer.contains("BERT"), "actual: " + answer);
    }
    
    @Test
    public void testEmbeddingMode() {
        String url = baseUrl + "similarity?" +
                     "text1=I like Jarkata EE and MicroProfile.&" + 
                     "text2=I like Python language.";
        Response response = client.target(url).request().get();
        JsonObject json = response.readEntity(JsonObject.class);
        
        double score = json.getJsonNumber("relevance-score").doubleValue();
        assertTrue(score > 0.69 && score < 0.70, "actual score: " + score);

        double similarity = json.getJsonNumber("similarity").doubleValue();
        assertTrue(similarity > 0.38 && similarity < 0.39,
                    "actual similarity: " + similarity);
    }
    
}
