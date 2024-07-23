import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import org.testcontainers.qdrant.QdrantContainer;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static dev.langchain4j.internal.Utils.randomUUID;

public class QdrantEmbeddingStoreExample {

  private static int grpcPort = 6334;
  private static String collectionName = "langchain4j-" + randomUUID();
  private static Collections.Distance distance = Collections.Distance.Cosine;
  private static int dimension = 384;

  public static void main(String[] args) throws ExecutionException, InterruptedException {

    try (QdrantContainer qdrant = new QdrantContainer("qdrant/qdrant:latest")) {
      qdrant.start();

      EmbeddingStore<TextSegment> embeddingStore =
              QdrantEmbeddingStore.builder()
                      .host(qdrant.getHost())
                      .port(qdrant.getMappedPort(grpcPort))
                      .collectionName(collectionName)
                      .build();

      QdrantClient client =
              new QdrantClient(
                      QdrantGrpcClient.newBuilder(qdrant.getHost(), qdrant.getMappedPort(grpcPort), false)
                              .build());

      client
              .createCollectionAsync(
                      collectionName,
                      Collections.VectorParams.newBuilder().setDistance(distance).setSize(dimension).build())
              .get();

      EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

      TextSegment segment1 = TextSegment.from("I've been to France twice.");
      Embedding embedding1 = embeddingModel.embed(segment1).content();
      embeddingStore.add(embedding1, segment1);

      TextSegment segment2 = TextSegment.from("New Delhi is the capital of India.");
      Embedding embedding2 = embeddingModel.embed(segment2).content();
      embeddingStore.add(embedding2, segment2);

      Embedding queryEmbedding = embeddingModel.embed("Did you ever travel abroad?").content();
      List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, 1);
      EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

      System.out.println(embeddingMatch.score());
      System.out.println(embeddingMatch.embedded().text());
    }
  }
}
