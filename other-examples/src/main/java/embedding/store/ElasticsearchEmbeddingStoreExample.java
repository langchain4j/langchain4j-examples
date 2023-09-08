package embedding.store;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.inprocess.InProcessEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;

import java.util.List;

import static dev.langchain4j.model.inprocess.InProcessEmbeddingModelType.ALL_MINILM_L6_V2;

public class ElasticsearchEmbeddingStoreExample {

    public static void main(String[] args) {
        /*
         * To run this example, ensure you have Elasticsearch running locally. If not, then:
         *   - Execute "docker pull docker.elastic.co/elasticsearch/elasticsearch:8.9.0"
         *   - Execute "docker run -d -p 9200:9200 -p 9300:9300 -e discovery.type=single-node -e xpack.security.enabled=false docker.elastic.co/elasticsearch/elasticsearch:8.9.0"
         *   - Wait until Elasticsearch is ready to serve (may take a few minutes)
         */
        EmbeddingStore<TextSegment> embeddingStore = ElasticsearchEmbeddingStore.builder()
                .serverUrl("http://localhost:9200")
                .indexName("test-index")
                .build();

        InProcessEmbeddingModel embeddingModel = new InProcessEmbeddingModel(ALL_MINILM_L6_V2);

        TextSegment segment1 = TextSegment.from("I like football.");
        Embedding embedding1 = embeddingModel.embed(segment1);
        embeddingStore.add(embedding1, segment1);

        TextSegment segment2 = TextSegment.from("The weather is good today.");
        Embedding embedding2 = embeddingModel.embed(segment2);
        embeddingStore.add(embedding2, segment2);

        // wait 2 seconds to let add method complete bulk TextSegment
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            // ignored
        }

        Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?");
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, 1);
        EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

        System.out.println(embeddingMatch.score()); // 0.8144288495729757
        System.out.println(embeddingMatch.embedded().text()); // I like football.
    }
}
