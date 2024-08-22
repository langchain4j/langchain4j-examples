import static dev.langchain4j.internal.Utils.randomUUID;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import java.util.List;
import org.testcontainers.chromadb.ChromaDBContainer;

public class ChromaEmbeddingStoreExample {

    public static void main(String[] args) {
        try (ChromaDBContainer chroma = new ChromaDBContainer("chromadb/chroma:0.5.2")) {
            chroma.start();

            EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore
                .builder()
                .baseUrl(chroma.getEndpoint())
                .collectionName(randomUUID())
                .logRequests(true)
                .logResponses(true)
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

            System.out.println(embeddingMatch.score()); // 0.8144288493114709
            System.out.println(embeddingMatch.embedded().text()); // I like football.

            embeddingStore.removeAll();
        }
    }
}
