import static java.util.Arrays.asList;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.vespa.VespaEmbeddingStore;
import java.util.List;

/**
 * Example of integration with Vespa. You need to configure Vespa server side first, instructions are
 * inside of README.md file.
 */
public class VespaEmbeddingStoreExample {

  public static void main(String[] args) {
    EmbeddingStore<TextSegment> embeddingStore = VespaEmbeddingStore
      .builder()
      // server url, e.g. https://alexey-heezer.langchain4j.mytenant346.aws-us-east-1c.dev.z.vespa-app.cloud
      .url("url")
      // local path to the SSL private key file,
      // e.g. /Users/user/.vespa/mytenant346.langchain4j.alexey-heezer/data-plane-private-key.pem
      .keyPath("keyPath")
      // local path to the SSL certificate file,
      // e.g. /Users/user/.vespa/mytenant346.langchain4j.alexey-heezer/data-plane-public-cert.pem
      .certPath("certPath")
      .build();

    EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    TextSegment segment1 = TextSegment.from("I like football.");
    Embedding embedding1 = embeddingModel.embed(segment1).content();
    embeddingStore.add(embedding1, segment1);

    TextSegment segment2 = TextSegment.from("I've never been to New York.");
    Embedding embedding2 = embeddingModel.embed(segment2).content();
    embeddingStore.add(embedding2, segment2);

    TextSegment segment3 = TextSegment.from(
      "But actually we tried our new swimming pool yesterday and it was awesome!"
    );
    Embedding embedding3 = embeddingModel.embed(segment3).content();
    embeddingStore.add(embedding3, segment3);

    List<String> ids = embeddingStore.addAll(
      asList(embedding1, embedding2, embedding3),
      asList(segment1, segment2, segment3)
    );

    System.out.println("added/updated records count: " + ids.size()); // 3

    TextSegment segment4 = TextSegment.from(
      "John Lennon was a very cool person."
    );
    Embedding embedding4 = embeddingModel.embed(segment4).content();
    String s4id = embeddingStore.add(embedding4, segment4);

    System.out.println("segment 4 id: " + s4id);

    Embedding queryEmbedding = embeddingModel.embed(
      "What is your favorite sport?"
    ).content();
    List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(
      queryEmbedding,
      2
    );

    System.out.println(
      "relevant results count for sport question: " + relevant.size()
    ); // 2

    System.out.println(relevant.get(0).score()); // 0.639...
    System.out.println(relevant.get(0).embedded().text()); // football
    System.out.println(relevant.get(1).score()); // 0.232...
    System.out.println(relevant.get(1).embedded().text()); // swimming pool

    queryEmbedding = embeddingModel.embed("And what about musicians?").content();
    relevant = embeddingStore.findRelevant(queryEmbedding, 5, 0.3);

    System.out.println(
      "relevant results count for music question: " + relevant.size()
    ); // 1

    System.out.println(relevant.get(0).score()); // 0.359...
    System.out.println(relevant.get(0).embedded().text()); // John Lennon
  }
}
