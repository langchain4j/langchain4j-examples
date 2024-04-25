
import com.datastax.oss.driver.api.core.CqlSession;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.cassio.SimilarityMetric;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.cassandra.CassandraCassioEmbeddingStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_3_5_TURBO;
import static dev.langchain4j.model.openai.OpenAiEmbeddingModelName.TEXT_EMBEDDING_ADA_002;
import static java.time.Duration.ofSeconds;

@Testcontainers
class CassandraNaiveRagTestIT {

    static final String VAR_OPENAI_API_KEY = "OPENAI_API_KEY";

    static final String CASSANDRA_IMAGE = "cassandra:5.0";
    static final String DATACENTER      = "datacenter1";
    static final String CLUSTER         = "langchain4j";
    static final String VECTOR_STORE    = "test_langchain4j";

    static CassandraContainer<?> cassandraContainer;

    /**
     * Check Docker is installed and running on host
     */
    @BeforeAll
    static void ensureDockerIsRunning() {
        DockerClientFactory.instance().client();
        if (cassandraContainer == null) {
            cassandraContainer = new CassandraContainer<>(
                    DockerImageName.parse(CASSANDRA_IMAGE))
                    .withEnv("CLUSTER_NAME", CLUSTER)
                    .withEnv("DC", DATACENTER);
            cassandraContainer.start();

            // Part of Database Creation, creating keyspace
            final InetSocketAddress contactPoint = cassandraContainer.getContactPoint();
            CqlSession.builder()
                    .addContactPoint(contactPoint)
                    .withLocalDatacenter(DATACENTER)
                    .build().execute(
                            "CREATE KEYSPACE IF NOT EXISTS " + CLUSTER +
                                    " WITH replication = {'class':'SimpleStrategy', 'replication_factor':'1'};");
        }
    }

    /**
     * Stop Cassandra Node
     */
    @AfterAll
    static void afterTests() throws Exception {
        cassandraContainer.stop();
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = "sk.*")
    void shouldRagWithOpenAiAndAstra() {
        // Parsing input file
        Path textFile = new File(Objects.requireNonNull(getClass()
                        .getResource("/story-about-happy-carrot.txt"))
                .getFile())
                .toPath();

        // === INGESTION ===

        EmbeddingModel embeddingModel = initEmbeddingModelOpenAi();
        EmbeddingStore<TextSegment> embeddingStore = initEmbeddingStoreCassandra();
        EmbeddingStoreIngestor.builder()
                .documentSplitter(recursive(100, 10, new OpenAiTokenizer(GPT_3_5_TURBO)))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build()
                .ingest(loadDocument(textFile, new TextDocumentParser()));

        // === NAIVE RETRIEVER ===

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.5)
                .build();

        CassandraAssistant ai = AiServices.builder(CassandraAssistant.class)
                .contentRetriever(contentRetriever)
                .chatLanguageModel(initChatLanguageModelOpenAi())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        String response = ai.answer("What vegetable is Happy?");
        Assertions.assertNotNull(response);

    }

    private EmbeddingStore<TextSegment> initEmbeddingStoreCassandra() {
        return CassandraCassioEmbeddingStore.builder()
                    .contactPoints(Collections.singletonList(cassandraContainer.getContactPoint().getHostName()))
                    .port(cassandraContainer.getContactPoint().getPort())
                    .localDataCenter(DATACENTER)
                    .keyspace(CLUSTER)
                    .table(VECTOR_STORE)
                    .dimension(1536)
                    .metric(SimilarityMetric.COSINE)
                    .build();
    }

    private ChatLanguageModel initChatLanguageModelOpenAi() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv(VAR_OPENAI_API_KEY))
                .modelName(GPT_3_5_TURBO)
                .temperature(0.7)
                .timeout(ofSeconds(15))
                .maxRetries(3)
                .logResponses(true)
                .logRequests(true)
                .build();
    }

    private EmbeddingModel initEmbeddingModelOpenAi() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv(VAR_OPENAI_API_KEY))
                .modelName(TEXT_EMBEDDING_ADA_002)
                .build();
    }
}