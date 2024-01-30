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
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_3_5_TURBO;

public class _02_Advanced_RAG_with_Query_Compression {

    /**
     * Please refer to previous examples for basic context.
     * <p>
     * Advanced RAG in LangChain4j is described here: https://github.com/langchain4j/langchain4j/pull/538
     * <p>
     * This example illustrates the implementation of a more sophisticated RAG application
     * using a technique known as "query compression".
     * Often, a query from a user is a follow-up question that refers back to earlier parts of the conversation
     * and lacks all the necessary details for effective retrieval.
     * For example, consider this conversation:
     * User: What is the legacy of John Doe?
     * AI: John Doe was a...
     * User: When was he born?
     * <p>
     * In such scenarios, using a basic RAG approach with a query like "When was he born?"
     * would likely fail to find articles about John Doe, as it doesn't contain "John Doe" in the query.
     * Query compression involves taking the user's query and the preceding conversation, then asking the LLM
     * to "compress" this into a single, self-contained query.
     * The LLM should generate a query like "When was John Doe born?".
     * This method adds a bit of latency and cost but significantly enhances the quality of the RAG process.
     * It's worth noting that the LLM used for compression doesn't have to be the same as the one
     * used for conversation. For instance, you might use a smaller local model trained for summarization.
     * <p>
     * In this example, we will continue using {@link AiServices},
     * but the same principles apply to {@link ConversationalRetrievalChain}, or you can develop your custom RAG flow.
     */

    public static void main(String[] args) {

        Biographer biographer = createBiographer();

        // First, ask "What is the legacy of John Doe?"
        // Then, ask "When was he born?"
        // Now, review the logs:
        // The first query was not compressed as there was no preceding context to compress.
        // The second query, however, was compressed into something like "When was John Doe born?"

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("==================================================");
                System.out.print("User: ");
                String userQuery = scanner.nextLine();
                System.out.println("==================================================");

                if ("exit".equalsIgnoreCase(userQuery)) {
                    break;
                }

                String biographerAnswer = biographer.answer(userQuery);
                System.out.println("==================================================");
                System.out.println("Biographer: " + biographerAnswer);
            }
        }
    }

    private static Biographer createBiographer() {

        // Check _01_Naive_RAG if you need more details on what is going on here

        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey("demo")
                .modelName(GPT_3_5_TURBO)
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        Path documentPath = toPath("biography-of-john-doe.txt");
        EmbeddingStore<TextSegment> embeddingStore = embed(documentPath, embeddingModel);

        // We will create a CompressingQueryTransformer, which is responsible for compressing
        // the user's query and the preceding conversation into a single, stand-alone query.
        // This should significantly improve the quality of the retrieval process.
        QueryTransformer queryTransformer = new CompressingQueryTransformer(chatModel);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();

        // The RetrievalAugmentor serves as the entry point into the RAG flow in LangChain4j.
        // It can be configured to customize the RAG behavior according to your requirements.
        // In subsequent examples, we will explore more customizations.
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .contentRetriever(contentRetriever)
                .build();

        return AiServices.builder(Biographer.class)
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

    interface Biographer {

        String answer(String query);
    }

    private static Path toPath(String fileName) {
        try {
            URL fileUrl = _02_Advanced_RAG_with_Query_Compression.class.getResource(fileName);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}