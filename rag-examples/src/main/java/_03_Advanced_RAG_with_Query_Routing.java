import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class _03_Advanced_RAG_with_Query_Routing {

    /**
     * Please refer to previous examples for basic context.
     * <p>
     * Advanced RAG in LangChain4j is described here: https://github.com/langchain4j/langchain4j/pull/538
     * <p>
     * This example showcases the implementation of a more advanced RAG application
     * using a technique known as "query routing".
     * <p>
     * Often, private data is spread across multiple sources and formats.
     * This might include internal company documentation on Confluence, your project's code in a Git repository,
     * a relational database with user data, or a search engine with the products you sell, among others.
     * In a RAG flow that utilizes data from multiple sources, you will likely have multiple
     * {@link EmbeddingStore}s or {@link ContentRetriever}s.
     * While you could route each user query to all available {@link ContentRetriever}s,
     * this approach might be inefficient and counterproductive.
     * <p>
     * "Query routing" is the solution to this challenge. It involves directing a query to the most appropriate
     * {@link ContentRetriever} (or several). Routing can be implemented in various ways:
     * - Using rules (e.g., depending on the user's privileges, location, etc.).
     * - Using keywords (e.g., if a query contains words X1, X2, X3, route it to {@link ContentRetriever} X, etc.).
     * - Using semantic similarity (see EmbeddingModelTextClassifierExample in this repository).
     * - Using an LLM to make a routing decision.
     * <p>
     * For scenarios 1, 2, and 3, you can implement a custom {@link QueryRouter}.
     * For scenario 4, this example will demonstrate how to use a {@link LanguageModelQueryRouter}.
     * <p>
     * We will continue using {@link AiServices} for this example,
     * but the same principles apply to {@link ConversationalRetrievalChain}, or you can develop your custom RAG flow.
     */

    public static void main(String[] args) {

        Polymath polymath = createPolymath();

        // First, ask "What is the legacy of John Doe?"
        // Then, ask "Can I cancel my reservation?"
        // Now, see the logs to observe how the queries are routed to different retrievers.

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("==================================================");
                System.out.print("User: ");
                String userQuery = scanner.nextLine();
                System.out.println("==================================================");

                if ("exit".equalsIgnoreCase(userQuery)) {
                    break;
                }

                String polymathAnswer = polymath.answer(userQuery);
                System.out.println("==================================================");
                System.out.println("Polymath: " + polymathAnswer);
            }
        }
    }

    private static Polymath createPolymath() {

        // Check _01_Naive_RAG if you need more details on what is going on here

        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey("demo")
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        // Let's create a separate embedding store specifically for biographies.
        EmbeddingStore<TextSegment> biographyEmbeddingStore =
                embed(toPath("biography-of-john-doe.txt"), embeddingModel);
        ContentRetriever biographyContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(biographyEmbeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();

        // Additionally, let's create a separate embedding store dedicated to terms of use.
        EmbeddingStore<TextSegment> termsOfUseEmbeddingStore =
                embed(toPath("miles-of-smiles-terms-of-use.txt"), embeddingModel);
        ContentRetriever termsOfUseContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(termsOfUseEmbeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();

        // Let's create a query router.
        Map<ContentRetriever, String> retrieverToDescription = new HashMap<>();
        retrieverToDescription.put(biographyContentRetriever, "biography of John Doe");
        retrieverToDescription.put(termsOfUseContentRetriever, "terms of use of car rental company");
        QueryRouter queryRouter = new LanguageModelQueryRouter(chatModel, retrieverToDescription);

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(queryRouter)
                .build();

        return AiServices.builder(Polymath.class)
                .chatLanguageModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    private static EmbeddingStore<TextSegment> embed(Path documentPath, EmbeddingModel embeddingModel) {
        DocumentParser documentParser = new TextDocumentParser();
        Document document = FileSystemDocumentLoader.loadDocument(documentPath, documentParser);

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.split(document);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);
        return embeddingStore;
    }

    interface Polymath {

        String answer(String query);
    }

    private static Path toPath(String fileName) {
        try {
            URL fileUrl = _03_Advanced_RAG_with_Query_Routing.class.getResource(fileName);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}