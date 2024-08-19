import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static dev.ai4j.openai4j.image.ImageModel.DALL_E_QUALITY_HD;
import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

public class OpenAiImageModelExamples {

    static class Simple_Prompt {

        public static void main(String[] args) {

            ImageModel model = OpenAiImageModel.withApiKey(System.getenv("OPENAI_API_KEY"));

            Response<Image> response = model.generate("Donald Duck in New York, cartoon style");

            System.out.println(response.content().url()); // Donald Duck is here :)
        }
    }

    static class Draw_Story_From_My_Document {

        public static void main(String[] args) throws URISyntaxException {

            ImageModel model = OpenAiImageModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .quality(DALL_E_QUALITY_HD)
                    .logRequests(true)
                    .logResponses(true)
                    .withPersisting()
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
                                            OpenAiImageModelExamples.class.getResource("example-files/story-about-happy-carrot.txt")
                                    )
                                    .toURI()
                    ),
                    new TextDocumentParser()
            );
            ingestor.ingest(document);

            ConversationalRetrievalChain chain = ConversationalRetrievalChain
                    .builder()
                    .chatLanguageModel(OpenAiChatModel.builder().apiKey(System.getenv("OPENAI_API_KEY")).build())
                    .contentRetriever(new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel))
                    .build();

            PromptTemplate drawPromptTemplate = PromptTemplate.from(
                    "Draw {{object}}. Base the picture on following information:\n\n{{information}}"
            );

            ImageModel model = OpenAiImageModel.withApiKey(System.getenv("OPENAI_API_KEY"));

            Response<Image> response = model.generate("Donald Duck in New York, cartoon style");

            System.out.println(response.content().url()); // Donald Duck is here :)
    }
}
