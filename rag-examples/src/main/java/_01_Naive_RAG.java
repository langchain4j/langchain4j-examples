import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
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


public class _01_Naive_RAG {

    /**
     * This example demonstrates how to implement a simple Retrieval-Augmented Generation (RAG) application.
     * By "simple," we mean that we won't use any advanced RAG techniques.
     * In each interaction with the Large Language Model (LLM), we will:
     * 1. Take the user's query as-is.
     * 2. Embed it using an embedding model.
     * 3. Use the query's embedding to search an embedding store (containing small segments of your documents)
     * for the X most relevant segments.
     * 4. Append the found segments to the user's query.
     * 5. Send the combined input (user query + segments) to the LLM.
     * 6. Hope that:
     * - The user's query is well-formulated and contains all necessary details for retrieval.
     * - The found segments are relevant to the user's query.
     * <p>
     * RAG can be implemented in LangChain4j in two ways:
     * <p>
     * 1. Using high-level components, such as:
     * 1.1 {@link AiServices} (supports RAG and other features like memory, tools, output parsers, etc.)
     * 1.2 {@link ConversationalRetrievalChain} (supports only RAG and memory).
     * <p>
     * 2. Using (or reusing) low-level components, such as:
     * - {@link RetrievalAugmentor} (e.g., {@link DefaultRetrievalAugmentor})
     * - {@link ContentRetriever} (e.g., {@link EmbeddingStoreContentRetriever})
     * - and others.
     * <p>
     * In this example, we will demonstrate how to use RAG with {@link AiServices}.
     */

    public static void main(String[] args) {

        // Let's create a customer support agent to chat about terms of use.
        CustomerSupportAgent agent = createCustomerSupportAgent();

        // Now, you can ask questions such as
        // - "Can I cancel my reservation?"
        // - "I had an accident, should I pay extra?"

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("==================================================");
                System.out.print("User: ");
                String userQuery = scanner.nextLine();
                System.out.println("==================================================");

                if ("exit".equalsIgnoreCase(userQuery)) {
                    break;
                }

                String agentAnswer = agent.answer(userQuery);
                System.out.println("==================================================");
                System.out.println("Agent: " + agentAnswer);
            }
        }
    }

    private static CustomerSupportAgent createCustomerSupportAgent() {

        // First, let's create a chat model, also known as a LLM, which will answer our queries.
        // In this example, we will use OpenAI's gpt-3.5-turbo, but you can choose any supported model.
        // Langchain4j currently supports more than 10 popular LLM providers.
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey("demo") // You can use the "demo" key or import your own.
                .modelName(GPT_3_5_TURBO)
                .build();


        // Now, let's load a document that we want to use for RAG.
        // We will use the terms of use from an imaginary car rental company, "Miles of Smiles".
        // For this example, we'll import only a single document, but you can load as many as you need.
        // LangChain4j offers built-in support for loading documents from various sources:
        // File System, URL, Amazon S3, Azure Blob Storage, GitHub, Tencent COS.
        // Additionally, LangChain4j supports parsing multiple document types:
        // text, pdf, doc, xls, ppt.
        // However, you can also manually import your data from other sources.
        Path documentPath = toPath("miles-of-smiles-terms-of-use.txt");
        DocumentParser documentParser = new TextDocumentParser();
        Document document = FileSystemDocumentLoader.loadDocument(documentPath, documentParser);


        // Now, we need to split this document into smaller segments, also known as "chunks."
        // This approach allows us to send only relevant segments to the LLM in response to a user query,
        // rather than the entire document. For instance, if a user asks about cancellation policies,
        // we will identify and send only those segments related to cancellation.
        // A good starting point is to use a recursive document splitter that initially attempts
        // to split by paragraphs. If a paragraph is too large to fit into a single segment,
        // the splitter will recursively divide it by newlines, then by sentences, and finally by words,
        // if necessary, to ensure each piece of text fits into a single segment.
        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.split(document);


        // Now, we need to embed (also known as "vectorize") these segments.
        // Embedding is needed for performing similarity searches.
        // For this example, we'll use a local in-process embedding model, but you can choose any supported model.
        // Langchain4j currently supports more than 10 popular embedding model providers.
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();


        // Next, we will store these embeddings in an embedding store (also known as a "vector database").
        // This store will be used to search for relevant segments during each interaction with the LLM.
        // For simplicity, this example uses an in-memory embedding store, but you can choose from any supported store.
        // Langchain4j currently supports more than 15 popular embedding stores.
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);


        // The content retriever is responsible for retrieving relevant content based on a user query.
        // Currently, it is capable of retrieving text segments, but future enhancements will include support for
        // additional modalities like images, audio, and more.
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2) // on each interaction we will retrieve the 2 most relevant segments
                .minScore(0.5) // we want to retrieve segments at least somewhat similar to user query
                .build();


        // Optionally, we can use a chat memory, enabling back-and-forth conversation with the LLM
        // and allowing it to remember previous interactions.
        // Currently, LangChain4j offers two chat memory implementations:
        // MessageWindowChatMemory and TokenWindowChatMemory.
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);


        // The final step is to build our AI Service,
        // configuring it to use the components we've created above.
        return AiServices.builder(CustomerSupportAgent.class)
                .chatLanguageModel(chatModel)
                .contentRetriever(contentRetriever)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * This is an "AI Service". It is a Java service with AI capabilities/features.
     * It can be integrated into your code like any other service, acting as a bean, and is even mockable.
     * The goal is to seamlessly integrate AI functionality into your (existing) codebase with minimal friction.
     * It's conceptually similar to Spring Data JPA or Retrofit.
     * You define an interface and optionally customize it with annotations.
     * LangChain4j then provides an implementation for this interface using proxy and reflection.
     * This approach abstracts away all the complexity and boilerplate.
     * So you won't need to juggle the model, messages, memory, RAG components, tools, output parsers, etc.
     * However, don't worry. It's quite flexible and configurable, so you'll be able to tailor it
     * to your specific use case.
     */
    interface CustomerSupportAgent {

        String answer(String query);
    }

    static Path toPath(String fileName) {
        try {
            URL fileUrl = _01_Naive_RAG.class.getResource(fileName);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}