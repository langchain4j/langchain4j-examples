package dev.langchain4j.example.gemini;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.output.Response;

/**
 * Demonstrates generating embeddings with Google AI Gemini.
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Create a {@link GoogleAiEmbeddingModel}</li>
 *   <li>Generate an embedding vector from text</li>
 *   <li>Access embedding dimensions and values</li>
 * </ul>
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 *
 * <p>Learn <a href="https://ai.google.dev/gemini-api/docs/embeddings>more</a></p>
 */
public class Example03_SimpleEmbedding {

    public static void main(String[] args) {
        GoogleAiEmbeddingModel model = GoogleAiEmbeddingModel.builder()
                .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
                .modelName("gemini-embedding-001")
                .outputDimensionality(1536)
                .build();

        Response<Embedding> response = model.embed("The quick brown fox jumps over the lazy dog.");

        Embedding embedding = response.content();

        System.out.println("Embedding dimension: " + embedding.dimension());
        System.out.println("First 10 values: ");
        for (int i = 0; i < 10; i++) {
            System.out.printf("  [%d]: %.6f%n", i, embedding.vector()[i]);
        }
    }
}