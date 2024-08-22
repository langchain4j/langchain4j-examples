import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.testcontainers.milvus.MilvusContainer;

import java.util.List;

public class MilvusEmbeddingStoreExample {

    public static void main(String[] args) {

        try (MilvusContainer milvus = new MilvusContainer("milvusdb/milvus:v2.3.1")) {
            milvus.start();
            EmbeddingStore<TextSegment> embeddingStore = MilvusEmbeddingStore.builder()
                    .uri(milvus.getEndpoint())
                    .collectionName("test_collection")
                    .dimension(384)
                    .build();

            EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

            TextSegment segment1 = TextSegment.from("I like football.");
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("The weather is good today.");
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
            List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, 1);
            EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

            System.out.println(embeddingMatch.score()); // 0.8144287765026093
            System.out.println(embeddingMatch.embedded().text()); // I like football.
        }
    }
}
