package _3_advanced;

import _2_naive.Naive_RAG_Example;
import dev.langchain4j.community.store.embedding.neo4j.Neo4jEmbeddingStore;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.experimental.rag.content.retriever.sql.SqlDatabaseContentRetriever;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.h2.jdbcx.JdbcDataSource;
import shared.Assistant;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static shared.Utils.*;
import static shared.Utils.startConversationWith;

import dev.langchain4j.data.document.Document;

import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class AdvancedRetrievalQaWithNeo4jExample {

    public static void main(String[] args) {

        shared.Assistant assistant = createAssistant("documents/biography-of-john-doe.txt");

        // First, ask "What is the legacy of John Doe?"
        // Then, ask "When was he born?"
        // Now, review the logs:
        // The first query was not compressed as there was no preceding context to compress.
        // The second query, however, was compressed into something like "When was John Doe born?"
        startConversationWith(assistant);
    }

    static Assistant createAssistant(String documentPath) {
        Document document = loadDocument(toPath(documentPath), new TextDocumentParser());
        
        // Set up models
        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("text-embedding-3-small")
                .dimensions(512)
                .timeout(Duration.ofSeconds(60))
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.withApiKey(System.getenv("OPENAI_API_KEY"));

        // Load & split documents
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);

        List<TextSegment> splitDocuments = splitter.splitAll(documents);

        // Ingest into Neo4j
        Neo4jEmbeddingStore embeddingStore = Neo4jEmbeddingStore.builder()
                .withBasicAuth("bolt://localhost:7687", "neo4j", "password")
                .build();

        Neo4jEmbeddingStoreIngestor.ingest(splitDocuments, embeddingModel, embeddingStore);

        // Create retriever and chain
        EmbeddingStoreRetriever retriever = EmbeddingStoreRetriever.from(embeddingStore, embeddingModel);

        RetrievalQAChain chain = RetrievalQaChain.builder()
                .chatLanguageModel(chatModel)
                .retriever(retriever)
                .build();

        // Bind chain to an Assistant interface
        return AiServices.create(Assistant.class, chain);
    }

    static void startConversationWith(Assistant assistant) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine();
            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("Goodbye!");
                break;
            }

            String response = assistant.answer(input);
            System.out.println("Assistant: " + response);
        }
    }

    public interface Assistant {
        String answer(String question);
    }
}
