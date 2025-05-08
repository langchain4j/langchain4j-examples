import dev.langchain4j.community.store.embedding.neo4j.Neo4jEmbeddingStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.testcontainers.containers.Neo4jContainer;

import java.util.List;

public class Neo4jEmbeddingStoreExample {

    private static EmbeddingStore<TextSegment> minimalEmbedding;
    private static final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    
    public static void main(String[] args) {
        try (Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.26")) {
            neo4j.start();
            minimalEmbedding = Neo4jEmbeddingStore.builder()
                    .withBasicAuth(neo4j.getBoltUrl(), "neo4j", neo4j.getAdminPassword())
                    .dimension(embeddingModel.dimension())
                    .build();

            searchEmbeddingsWithSingleMaxResult(minimalEmbedding);
            searchEmbeddingsWithAddAllAndSingleMaxResult();
            searchEmbeddingsWithAddAllWithMetadataMaxResultsAndMinScore();
            
            // custom embeddingStore
            Neo4jEmbeddingStore customEmbeddingStore = Neo4jEmbeddingStore.builder()
                    .withBasicAuth(neo4j.getBoltUrl(), "neo4j", neo4j.getAdminPassword())
                    .dimension(embeddingModel.dimension())
                    .indexName("customidx")
                    .label("CustomLabel")
                    .embeddingProperty("customProp")
                    .idProperty("customId")
                    .textProperty("customText")
                    .build();
            searchEmbeddingsWithSingleMaxResult(customEmbeddingStore);
        }
    }

    private static void searchEmbeddingsWithSingleMaxResult(EmbeddingStore<TextSegment> minimalEmbedding) {
        
        TextSegment segment1 = TextSegment.from("I like football.");
        Embedding embedding1 = embeddingModel.embed(segment1).content();
        minimalEmbedding.add(embedding1, segment1);

        TextSegment segment2 = TextSegment.from("The weather is good today.");
        Embedding embedding2 = embeddingModel.embed(segment2).content();
        minimalEmbedding.add(embedding2, segment2);

        Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
        final EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(1)
                .build();
        List<EmbeddingMatch<TextSegment>> relevant = minimalEmbedding.search(request).matches();
        EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

        System.out.println(embeddingMatch.score()); // 0.8144289255142212
        System.out.println(embeddingMatch.embedded().text()); // I like football.
    }
    
    private static void searchEmbeddingsWithAddAllAndSingleMaxResult() {
        
        TextSegment segment1 = TextSegment.from("I like football.");
        Embedding embedding1 = embeddingModel.embed(segment1).content();

        TextSegment segment2 = TextSegment.from("The weather is good today.");
        Embedding embedding2 = embeddingModel.embed(segment2).content();

        TextSegment segment3 = TextSegment.from("I like basketball.");
        Embedding embedding3 = embeddingModel.embed(segment3).content();
        minimalEmbedding.addAll(
                List.of(embedding1, embedding2, embedding3),
                List.of(segment1, segment2, segment3)
        );

        Embedding queryEmbedding = embeddingModel.embed("What are your favourites sport?").content();
        final EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(1)
                .build();
        List<EmbeddingMatch<TextSegment>> relevant = minimalEmbedding.search(request).matches();

        relevant.forEach(match -> {
            System.out.println(match.score()); // 0.8144289255142212
            System.out.println(match.embedded().text()); // I like football. ||  I like basketball.
        });
        
    }

    private static void searchEmbeddingsWithAddAllWithMetadataMaxResultsAndMinScore() {
        
        TextSegment segment1 = TextSegment.from("I like football.", Metadata.from("test-key-1", "test-value-1"));
        Embedding embedding1 = embeddingModel.embed(segment1).content();

        TextSegment segment2 = TextSegment.from("The weather is good today.", Metadata.from("test-key-2", "test-value-2"));
        Embedding embedding2 = embeddingModel.embed(segment2).content();

        TextSegment segment3 = TextSegment.from("I like basketball.", Metadata.from("test-key-3", "test-value-3"));
        Embedding embedding3 = embeddingModel.embed(segment3).content();
        minimalEmbedding.addAll(
                List.of(embedding1, embedding2, embedding3),
                List.of(segment1, segment2, segment3)
        );

        Embedding queryEmbedding = embeddingModel.embed("What are your favourite sports?").content();
        final EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(2)
                .minScore(0.15)
                .build();
        List<EmbeddingMatch<TextSegment>> relevant = minimalEmbedding.search(request).matches();
        relevant.forEach(match -> {
            System.out.println(match.score()); // 0.8144289255142212
            System.out.println(match.embedded().text()); // I like football. || I like basketball.
        });
    }
}
