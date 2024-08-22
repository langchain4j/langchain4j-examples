import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore;
import org.opensearch.testcontainers.OpensearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

public class OpenSearchEmbeddingStoreExample {

    public static void main(String[] args) throws InterruptedException {

        try (var opensearch = new OpensearchContainer(DockerImageName.parse("opensearchproject/opensearch:2.0.0"))) {
            opensearch.start();
            EmbeddingStore<TextSegment> embeddingStore = OpenSearchEmbeddingStore.builder()
                    .serverUrl(opensearch.getHttpHostAddress())
                    .build();

            EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

            TextSegment segment1 = TextSegment.from("I like football.");
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("The weather is good today.");
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            Thread.sleep(1000); // to be sure that embeddings were persisted

            Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
            List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, 1);
            EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

            System.out.println(embeddingMatch.score()); // 0.8144289
            System.out.println(embeddingMatch.embedded().text()); // I like football.
        }
    }
}
