import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.infinispan.InfinispanEmbeddingStore;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.server.test.core.InfinispanContainer;

import java.util.List;

import static org.infinispan.server.test.core.InfinispanContainer.DEFAULT_PASSWORD;
import static org.infinispan.server.test.core.InfinispanContainer.DEFAULT_USERNAME;

public class InfinispanEmbeddingStoreExample {

    public static void main(String[] args) {

        InfinispanContainer infinispan = new InfinispanContainer();
        infinispan.start();

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer().host(infinispan.getHost())
                .port(infinispan.getFirstMappedPort())
                .security()
                .authentication()
                .username(DEFAULT_USERNAME)
                .password(DEFAULT_PASSWORD);
        // just to avoid docker 4 mac issues, don't use in production!!
        builder.clientIntelligence(ClientIntelligence.BASIC);

        EmbeddingStore<TextSegment> embeddingStore = InfinispanEmbeddingStore.builder()
                .cacheName("my-cache")
                .dimension(384)
                .infinispanConfigBuilder(builder)
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        TextSegment segment1 = TextSegment.from("I like football.");
        Embedding embedding1 = embeddingModel.embed(segment1).content();
        embeddingStore.add(embedding1, segment1);

        TextSegment segment2 = TextSegment.from("The weather is good today.");
        Embedding embedding2 = embeddingModel.embed(segment2).content();
        embeddingStore.add(embedding2, segment2);

        Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(1)
                .build();
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(embeddingSearchRequest).matches();
        EmbeddingMatch<TextSegment> embeddingMatch = matches.get(0);

        System.out.println(embeddingMatch.score()); // 0.8144288659095
        System.out.println(embeddingMatch.embedded().text()); // I like football.

        infinispan.stop();
    }
}
