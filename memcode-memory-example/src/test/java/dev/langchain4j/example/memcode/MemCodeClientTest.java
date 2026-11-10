package dev.langchain4j.example.memcode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MemCodeClient}. A local HTTP stub stands in for the hosted API, so the
 * tests run without credentials and verify request shaping, response parsing and error mapping.
 */
class MemCodeClientTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private HttpServer server;
    private final AtomicReference<String> lastMethod = new AtomicReference<>();
    private final AtomicReference<String> lastPath = new AtomicReference<>();
    private final AtomicReference<String> lastAuthorization = new AtomicReference<>();
    private final AtomicReference<String> lastBody = new AtomicReference<>();

    private String baseUrl;
    private int responseStatus = 200;
    private String responseBody = "{}";

    @BeforeEach
    void startStubServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            lastMethod.set(exchange.getRequestMethod());
            lastPath.set(exchange.getRequestURI().getPath());
            lastAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            lastBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(responseStatus, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopStubServer() {
        server.stop(0);
    }

    private MemCodeClient client() {
        return new MemCodeClient(baseUrl, "mc_live_test_key");
    }

    @Test
    void searchPostsQueryWithBearerCredentialAndParsesHits() throws IOException {
        responseBody = """
                {"status":"ok","data":{"memory_results":[
                  {"domain":"profile","content":"prefers window seats","score":0.87}
                ]}}
                """;

        List<MemCodeClient.MemoryHit> hits = client().search("seat preference", 5);

        assertEquals("POST", lastMethod.get());
        assertEquals("/v2/memory/search", lastPath.get());
        assertEquals("Bearer mc_live_test_key", lastAuthorization.get());

        JsonNode request = JSON.readTree(lastBody.get());
        assertEquals("seat preference", request.path("query").asText());
        assertEquals("memories", request.path("mode").asText());
        assertEquals(5, request.path("top_k").asInt());
        assertEquals(false, request.path("include_original_chunks").asBoolean());

        assertEquals(1, hits.size());
        assertEquals("profile", hits.get(0).domain());
        assertEquals("prefers window seats", hits.get(0).content());
        assertEquals(0.87, hits.get(0).score());
    }

    @Test
    void searchBoundsTopK() throws IOException {
        responseBody = "{\"status\":\"ok\",\"data\":{\"memory_results\":[]}}";

        client().search("anything", 99);
        assertEquals(10, JSON.readTree(lastBody.get()).path("top_k").asInt());

        client().search("anything", 0);
        assertEquals(5, JSON.readTree(lastBody.get()).path("top_k").asInt());
    }

    @Test
    void searchParsesResultsAliasAndToleratesMissingScore() throws IOException {
        responseBody = "{\"status\":\"ok\",\"data\":{\"results\":[{\"content\":\"likes trains\"}]}}";

        List<MemCodeClient.MemoryHit> hits = client().search("transport", 3);

        assertEquals(1, hits.size());
        assertEquals("memory", hits.get(0).domain());
        assertEquals("likes trains", hits.get(0).content());
        assertNull(hits.get(0).score());
    }

    @Test
    void retrieveParsesAnswerConfidenceAndSources() {
        responseBody = """
                {"status":"ok","data":{
                  "answer":"The user prefers window seats.",
                  "confidence":0.72,
                  "sources":[{"domain":"profile","content":"prefers window seats","score":0.81}]
                }}
                """;

        MemCodeClient.RetrievedAnswer answer = client().retrieve("Which seat?", 5);

        assertEquals("POST", lastMethod.get());
        assertEquals("/v2/memory/retrieve", lastPath.get());
        assertEquals("The user prefers window seats.", answer.answer());
        assertEquals(0.72, answer.confidence());
        assertEquals(1, answer.sources().size());
        assertEquals("prefers window seats", answer.sources().get(0).content());
    }

    @Test
    void ingestPostsApprovedTextAndReturnsReceipt() throws IOException {
        responseBody = "{\"status\":\"ok\",\"data\":{\"job_id\":\"job-123\",\"status\":\"queued\"}}";

        MemCodeClient.IngestReceipt receipt = client().ingest("User is allergic to peanuts.");

        assertEquals("POST", lastMethod.get());
        assertEquals("/v2/memory/ingest", lastPath.get());
        JsonNode request = JSON.readTree(lastBody.get());
        assertEquals("User is allergic to peanuts.", request.path("user_query").asText());
        assertEquals("low", request.path("effort_level").asText());
        assertEquals(false, request.path("forget").asBoolean());

        assertEquals("job-123", receipt.jobId());
        assertEquals("queued", receipt.status());
    }

    @Test
    void ingestStatusPollsJobPathAndParsesStatus() {
        responseBody = "{\"status\":\"ok\",\"data\":{\"status\":\"completed\",\"progress\":100}}";

        MemCodeClient.IngestStatus status = client().ingestStatus("job-123");

        assertEquals("GET", lastMethod.get());
        assertEquals("/v2/memory/ingest/job-123/status", lastPath.get());
        assertEquals("completed", status.status());
        assertEquals("100", status.progress());
    }

    @Test
    void blankCredentialsAndTextAreRejected() {
        assertThrows(MemCodeClient.MemCodeException.class, () -> new MemCodeClient(baseUrl, "  "));
        assertThrows(MemCodeClient.MemCodeException.class, () -> client().ingest("   "));
        assertThrows(MemCodeClient.MemCodeException.class, () -> client().search("", 5));
        assertThrows(MemCodeClient.MemCodeException.class, () -> client().ingest("x".repeat(10_001)));
    }

    @Test
    void nonLoopbackHttpBaseUrlIsRejected() {
        MemCodeClient.MemCodeException exception = assertThrows(MemCodeClient.MemCodeException.class,
                () -> new MemCodeClient("http://memory.example.com", "mc_live_test_key"));
        assertTrue(exception.getMessage().contains("HTTPS"));

        // HTTPS endpoints and loopback stubs are accepted.
        new MemCodeClient("https://memory.memcode.in", "mc_live_test_key");
        new MemCodeClient(baseUrl, "mc_live_test_key");
    }

    @Test
    void errorEnvelopeIsSurfacedAsException() {
        responseBody = "{\"status\":\"error\",\"error\":\"credential is not authorized for this integration\"}";

        MemCodeClient.MemCodeException exception = assertThrows(MemCodeClient.MemCodeException.class,
                () -> client().search("anything", 5));
        assertTrue(exception.getMessage().contains("credential is not authorized for this integration"));
    }

    @Test
    void rejectedCredentialHasActionableMessage() {
        responseStatus = 401;
        responseBody = "{\"status\":\"error\",\"error\":\"invalid key\"}";

        MemCodeClient.MemCodeException exception = assertThrows(MemCodeClient.MemCodeException.class,
                () -> client().search("anything", 5));
        assertTrue(exception.getMessage().contains("rejected the configured credential"));
    }

    @Test
    void rateLimitAndServerErrorsHaveDedicatedMessages() {
        responseStatus = 429;
        responseBody = "{\"status\":\"error\",\"error\":\"slow down\"}";
        MemCodeClient.MemCodeException rateLimited = assertThrows(MemCodeClient.MemCodeException.class,
                () -> client().search("anything", 5));
        assertTrue(rateLimited.getMessage().contains("rate limit"));

        responseStatus = 503;
        responseBody = "unavailable";
        MemCodeClient.MemCodeException unavailable = assertThrows(MemCodeClient.MemCodeException.class,
                () -> client().search("anything", 5));
        assertTrue(unavailable.getMessage().contains("temporarily unavailable"));
    }
}
