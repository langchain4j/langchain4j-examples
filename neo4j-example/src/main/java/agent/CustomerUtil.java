package agent;

import dev.langchain4j.community.store.embedding.neo4j.Neo4jEmbeddingStore;
import dev.langchain4j.community.store.memory.chat.neo4j.Neo4jChatMemoryStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.testcontainers.containers.Neo4jContainer;
import util.Utils;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static util.Utils.toPath;

public class CustomerUtil {

    public static Utils.Assistant createAssistant(ChatModel chatModel, Neo4jChatMemoryStore chatMemoryStore) {
        return AiServices.builder(Utils.Assistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(sessionId -> MessageWindowChatMemory.builder()
                        .id(sessionId)
                        .chatMemoryStore(chatMemoryStore)
                        .maxMessages(10)
                        .build())
                .build();
    }

    public static Neo4jEmbeddingStore createEmbeddingStore(Neo4jContainer<?> neo4j, EmbeddingModel embeddingModel) {
        Neo4jEmbeddingStore embeddingStore = Neo4jEmbeddingStore.builder()
                .withBasicAuth(neo4j.getBoltUrl(), "neo4j", neo4j.getAdminPassword())
                .dimension(384)
                .build();
        
        Document document = loadDocument(toPath("miles-of-smiles-terms-of-use.txt"), new TextDocumentParser());
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(100, 0);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);
        return embeddingStore;
    }
    
    public static class AssistantService {

        private final Utils.Assistant assistant;
        private final Neo4jEmbeddingStore embeddingStore;
        private final EmbeddingModel embeddingModel;

        public AssistantService(Utils.Assistant assistant,
                                Neo4jEmbeddingStore embeddingStore,
                                EmbeddingModel embeddingModel) {
            this.assistant = assistant;
            this.embeddingStore = embeddingStore;
            this.embeddingModel = embeddingModel;
        }

        public String chat(String sessionId, String userMessage) {
            Embedding queryEmbedding = embeddingModel.embed(userMessage).content();
            final EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(3)
                    .build();
            final List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(request).matches();
            String context = matches.stream().map(i -> i.embedded().text()).collect(Collectors.joining("\n---\n"));

            String prompt = """
                    You are a helpful customer support agent.
                    Use the following context to answer the user:
                    %s
                    User: %s
                    """.formatted(context, userMessage);


            return assistant.chat(prompt);
        }
    }
}
