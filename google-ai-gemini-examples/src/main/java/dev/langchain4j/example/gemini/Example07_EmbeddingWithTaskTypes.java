package dev.langchain4j.example.gemini;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel.TaskType;
import dev.langchain4j.model.output.Response;

/**
 * Demonstrates embedding generation with different task types using Google AI Gemini.
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Configure embeddings for different use cases using {@link TaskType}</li>
 *   <li>Generate embeddings optimized for retrieval queries vs documents</li>
 *   <li>Compare embeddings across different task types</li>
 * </ul>
 *
 * <p>Task types include:
 * <ul>
 *   <li>{@code RETRIEVAL_QUERY} - for search queries</li>
 *   <li>{@code RETRIEVAL_DOCUMENT} - for documents to be searched</li>
 *   <li>{@code SEMANTIC_SIMILARITY} - for comparing text similarity</li>
 *   <li>{@code CLASSIFICATION} - for text classification tasks</li>
 *   <li>{@code CLUSTERING} - for grouping similar texts</li>
 * </ul>
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 *
 * <p>Learn <a href="https://ai.google.dev/gemini-api/docs/embeddings#supported-task-types">more</a></p>
 */
public class Example07_EmbeddingWithTaskTypes {

    public static void main(String[] args) {
        String apiKey = System.getenv("GOOGLE_AI_GEMINI_API_KEY");

        // Embedding for a search query
        GoogleAiEmbeddingModel queryModel = GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-embedding-001")
                .taskType(TaskType.RETRIEVAL_QUERY)
                .build();

        String query = "What is machine learning?";
        Response<Embedding> queryEmbedding = queryModel.embed(query);
        System.out.println("Query embedding (RETRIEVAL_QUERY):");
        System.out.println("  Text: \"" + query + "\"");
        System.out.println("  Dimension: " + queryEmbedding.content().dimension());

        // Embedding for a document to be searched
        GoogleAiEmbeddingModel documentModel = GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-embedding-001")
                .taskType(TaskType.RETRIEVAL_DOCUMENT)
                .build();

        String document = "Machine learning is a subset of artificial intelligence that enables " +
                "systems to learn and improve from experience without being explicitly programmed.";
        Response<Embedding> documentEmbedding = documentModel.embed(document);
        System.out.println("\nDocument embedding (RETRIEVAL_DOCUMENT):");
        System.out.println("  Text: \"" + document.substring(0, 50) + "...\"");
        System.out.println("  Dimension: " + documentEmbedding.content().dimension());

        // Embedding for semantic similarity comparison
        GoogleAiEmbeddingModel similarityModel = GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-embedding-001")
                .taskType(TaskType.SEMANTIC_SIMILARITY)
                .build();

        String text1 = "The cat sat on the mat.";
        String text2 = "A feline rested on the rug.";
        Response<Embedding> embedding1 = similarityModel.embed(text1);
        Response<Embedding> embedding2 = similarityModel.embed(text2);

        double similarity = cosineSimilarity(embedding1.content().vector(), embedding2.content().vector());

        System.out.println("\nSemantic similarity (SEMANTIC_SIMILARITY):");
        System.out.println("  Text 1: \"" + text1 + "\"");
        System.out.println("  Text 2: \"" + text2 + "\"");
        System.out.printf("  Cosine similarity: %.4f%n", similarity);
    }

    private static double cosineSimilarity(float[] a, float[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}