import static dev.langchain4j.data.document.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.image.openai.dalle.OpenAiDalleImageProcessingModel.DALL_E_QUALITY_HD;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.image.openai.dalle.OpenAiDalleImageProcessingModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.SneakyThrows;

public class OpenAiDalleImageProcessingExample {

  static class Simple_Prompt {

    public static void main(String[] args) {
      OpenAiDalleImageProcessingModel model = OpenAiDalleImageProcessingModel
        .builder()
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .build();

      Response<Image> image = model.generate("Donald Duck in New York, cartoon style");

      System.out.println(image.content().getUrl()); // Donald Duck is here :)
    }
  }

  static class Draw_My_File {

    @SneakyThrows
    public static void main(String[] args) {
      OpenAiDalleImageProcessingModel model = OpenAiDalleImageProcessingModel
        .builder()
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .quality(DALL_E_QUALITY_HD)
        .logRequests(true)
        .logResponses(true)
        .downloadFolder("/tmp")
        .build();

      EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
      EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

      EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor
        .builder()
        .documentSplitter(DocumentSplitters.recursive(1000, 0))
        .embeddingModel(embeddingModel)
        .embeddingStore(embeddingStore)
        .build();

      Document document = loadDocument(
        Paths.get(
          Objects
            .requireNonNull(
              OpenAiDalleImageProcessingExample.class.getResource("example-files/story-about-happy-carrot.txt")
            )
            .toURI()
        )
      );
      ingestor.ingest(document);

      ConversationalRetrievalChain chain = ConversationalRetrievalChain
        .builder()
        .chatLanguageModel(OpenAiChatModel.builder().apiKey(System.getenv("OPENAI_API_KEY")).build())
        .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
        .build();

      PromptTemplate drawPromptTemplate = PromptTemplate.from(
        "Draw {{object}}. Base the picture on following information:\n\n{{information}}"
      );

      Map<String, Object> variables = new HashMap<>();
      variables.put("information", chain.execute("Who is Charlie?"));
      variables.put("object", "Ultra realistic Charlie on the party, cinematic lighting");

      Response<Image> image = model.generate(drawPromptTemplate.apply(variables).text());

      System.out.println(image.content().getUrl()); // Enjoy your locally stored picture of Charlie on the party :)
    }
  }
}
