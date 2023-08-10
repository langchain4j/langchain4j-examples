package embedding.store;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.inprocess.InProcessEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;

import java.util.List;

import static dev.langchain4j.model.inprocess.InProcessEmbeddingModelType.ALL_MINILM_L6_V2;

public class WeaviateEmbeddingStoreExample {

    public static void main(String[] args) throws Exception {

        // Requires "langchain4j-weaviate" Maven/Gradle dependency

        EmbeddingStore<TextSegment> embeddingStore = WeaviateEmbeddingStore.builder()
                // Find it under "Show API keys" of your Weaviate cluster.
                .apiKey(System.getenv("WEAVIATE_API_KEY"))
                // The scheme, e.g. "https" of cluster URL. Find in under Details of your Weaviate cluster.
                .scheme("https")
                // The host, e.g. "langchain4j-4jw7ufd9.weaviate.network" of cluster URL.
                // Find in under Details of your Weaviate cluster.
                .host("test-182fuogk.weaviate.network")
                // Default class is used if not specified
                .objectClass("Test")
                // If true (default), then WeaviateEmbeddingStore will generate a hashed ID based on provided
                // text segment, which avoids duplicated entries in DB. If false, then random ID will be generated.
                .avoidDups(true)
                // Consistency level: ONE, QUORUM (default) or ALL.
                .consistencyLevel("ALL")
                .build();

        InProcessEmbeddingModel embeddingModel = new InProcessEmbeddingModel(ALL_MINILM_L6_V2);

        TextSegment segment1 = TextSegment.from("I like football.");
        Embedding embedding1 = embeddingModel.embed(segment1);
        embeddingStore.add(embedding1, segment1);

        TextSegment segment2 = TextSegment.from("The weather is good today.");
        Embedding embedding2 = embeddingModel.embed(segment2);
        embeddingStore.add(embedding2, segment2);

        Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?");
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, 1);
        EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

        System.out.println(embeddingMatch.score()); // 0.8144288063049316
        System.out.println(embeddingMatch.embedded().text()); // I like football.
    }
}
