package dev.langchain4j.example.memcode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal REST client for the MemCode personal v2 memory API
 * (default origin: <a href="https://memory.memcode.in">memory.memcode.in</a>).
 *
 * <p><b>Server-side user scoping.</b> The bearer credential identifies the memory owner.
 * This client intentionally sends no user id with any request: the identity is bound to the
 * API key server-side, so application code cannot read or write another user's memory by
 * passing a different id. Requests that try to override attribution are rejected by MemCode.
 *
 * <p>The client is kept small on purpose: production code can use any HTTP client, the request
 * and response shapes are documented in the module README.
 */
public class MemCodeClient {

    public static final String DEFAULT_BASE_URL = "https://memory.memcode.in";

    static final int MAX_TOP_K = 10;
    static final int DEFAULT_TOP_K = 5;
    static final int MAX_TEXT_LENGTH = 10_000;

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String apiKey;

    public MemCodeClient(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build());
    }

    MemCodeClient(String baseUrl, String apiKey, HttpClient httpClient) {
        this.baseUrl = validateBaseUrl(baseUrl);
        if (apiKey == null || apiKey.isBlank()) {
            throw new MemCodeException("The MemCode API key is blank. Create one at "
                    + "https://app.memcode.in/dashboard?section=api-keys");
        }
        this.apiKey = apiKey.trim();
        this.httpClient = httpClient;
    }

    /**
     * Reads the endpoint from {@code MEMCODE_API_URL} (default {@value #DEFAULT_BASE_URL}) and
     * the credential from {@code MEMCODE_API_KEY}.
     */
    public static MemCodeClient fromEnvironment() {
        String url = System.getenv("MEMCODE_API_URL");
        if (url == null || url.isBlank()) {
            url = DEFAULT_BASE_URL;
        }
        String key = System.getenv("MEMCODE_API_KEY");
        if (key == null || key.isBlank()) {
            throw new MemCodeException("MEMCODE_API_KEY is not set. Create a key at "
                    + "https://app.memcode.in/dashboard?section=api-keys and export it before running this example.");
        }
        return new MemCodeClient(url, key);
    }

    /**
     * Stores one user-approved fact. MemCode ingests asynchronously and returns a durable
     * receipt: a queued receipt is not proof that the memory is already retrievable, poll
     * {@link #ingestStatus(String)} to observe completion.
     */
    public IngestReceipt ingest(String text) {
        ObjectNode body = JSON.createObjectNode();
        body.put("user_query", requireText(text, "text"));
        body.put("effort_level", "low");
        body.put("forget", false);

        JsonNode data = post("/v2/memory/ingest", body);
        return new IngestReceipt(textOr(data, "job_id", ""), textOr(data, "status", "queued"));
    }

    /** Returns the current state of an ingest job previously returned by {@link #ingest(String)}. */
    public IngestStatus ingestStatus(String jobId) {
        String id = requireText(jobId, "job_id");
        JsonNode data = get("/v2/memory/ingest/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "/status");

        JsonNode progress = data.path("progress");
        return new IngestStatus(
                textOr(data, "status", "unknown"),
                progress.isMissingNode() || progress.isNull() ? null : progress.toString());
    }

    /**
     * Raw semantic search over the user's extracted memories. No model call happens inside
     * MemCode for this method, the returned entries are meant to be injected as reference
     * context before the application invokes its own assistant.
     */
    public List<MemoryHit> search(String query, int topK) {
        ObjectNode body = JSON.createObjectNode();
        body.put("query", requireText(query, "query"));
        body.put("mode", "memories");
        body.put("top_k", boundedTopK(topK));
        body.put("include_original_chunks", false);
        body.put("search_mode", "default");
        body.put("minimum_score", 0.0);

        JsonNode data = post("/v2/memory/search", body);
        JsonNode items = data.has("memory_results") ? data.get("memory_results") : data.path("results");
        return toHits(items);
    }

    /** Retrieval variant where MemCode answers the query from memory and returns source entries. */
    public RetrievedAnswer retrieve(String query, int topK) {
        ObjectNode body = JSON.createObjectNode();
        body.put("query", requireText(query, "query"));
        body.put("top_k", boundedTopK(topK));

        JsonNode data = post("/v2/memory/retrieve", body);
        JsonNode confidence = data.path("confidence");
        return new RetrievedAnswer(
                textOr(data, "answer", ""),
                confidence.isMissingNode() || confidence.isNull() ? null : confidence.asDouble(),
                toHits(data.path("sources")));
    }

    // ------------------------------------------------------------------
    // HTTP plumbing
    // ------------------------------------------------------------------

    private JsonNode post(String path, JsonNode body) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "langchain4j-memcode-example/0.1")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body), StandardCharsets.UTF_8))
                    .build();
        } catch (IOException e) {
            throw new MemCodeException("Could not serialize the MemCode request body.", e);
        }
        return send(request);
    }

    private JsonNode get(String path) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("User-Agent", "langchain4j-memcode-example/0.1")
                .GET()
                .build();
        return send(request);
    }

    private JsonNode send(HttpRequest request) {
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new MemCodeException("MemCode is unreachable at " + baseUrl + " (" + e.getMessage() + ").", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MemCodeException("Interrupted while calling MemCode.", e);
        }

        int status = response.statusCode();
        if (status == 401 || status == 403) {
            throw new MemCodeException("MemCode rejected the configured credential (HTTP " + status
                    + "). Check MEMCODE_API_KEY and its integration attribution.");
        }
        if (status == 429) {
            throw new MemCodeException("MemCode rate limit reached (HTTP 429). Retry later.");
        }
        if (status >= 500) {
            throw new MemCodeException("MemCode is temporarily unavailable (HTTP " + status + ").");
        }

        JsonNode payload;
        try {
            payload = JSON.readTree(response.body());
        } catch (IOException e) {
            throw new MemCodeException("MemCode returned a non-JSON response (HTTP " + status + ").", e);
        }
        if (payload == null || !payload.isObject()) {
            throw new MemCodeException("MemCode returned an invalid response envelope (HTTP " + status + ").");
        }
        if (status >= 400) {
            throw new MemCodeException("MemCode request failed with HTTP " + status + ": "
                    + textOr(payload, "error", "no error details"));
        }
        if ("error".equals(payload.path("status").asText(""))) {
            throw new MemCodeException("MemCode request failed: " + textOr(payload, "error", "unknown error"));
        }

        JsonNode data = payload.path("data");
        return data.isMissingNode() ? payload : data;
    }

    private static List<MemoryHit> toHits(JsonNode items) {
        List<MemoryHit> hits = new ArrayList<>();
        if (items == null || !items.isArray()) {
            return hits;
        }
        for (JsonNode item : items) {
            JsonNode score = item.path("score");
            hits.add(new MemoryHit(
                    textOr(item, "domain", "memory"),
                    textOr(item, "content", ""),
                    score.isMissingNode() || score.isNull() ? null : score.asDouble()));
        }
        return hits;
    }

    private static String validateBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new MemCodeException("The MemCode base URL is blank.");
        }
        String value = baseUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        URI uri = URI.create(value);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if ("https".equals(scheme)) {
            return value;
        }
        // Plain HTTP is accepted only for loopback endpoints, so the example can be tested
        // against a local stub. The hosted MemCode API must always be called over HTTPS.
        if ("http".equals(scheme) && host != null
                && (host.equals("localhost") || host.equals("127.0.0.1") || host.equals("[::1]") || host.equals("::1"))) {
            return value;
        }
        throw new MemCodeException("The MemCode base URL must use HTTPS.");
    }

    private static int boundedTopK(int topK) {
        if (topK <= 0) {
            return DEFAULT_TOP_K;
        }
        return Math.min(topK, MAX_TOP_K);
    }

    private static String requireText(String value, String field) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw new MemCodeException(field + " is required.");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new MemCodeException(field + " must be " + MAX_TEXT_LENGTH + " characters or fewer.");
        }
        return text;
    }

    private static String textOr(JsonNode node, String field, String fallback) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? fallback : value.asText();
    }

    // ------------------------------------------------------------------
    // Value types
    // ------------------------------------------------------------------

    /** One extracted memory entry. */
    public record MemoryHit(String domain, String content, Double score) {}

    /** Result of {@link #retrieve(String, int)}: an answer plus its source entries. */
    public record RetrievedAnswer(String answer, Double confidence, List<MemoryHit> sources) {}

    /** Durable ingest receipt returned by {@link #ingest(String)}. */
    public record IngestReceipt(String jobId, String status) {}

    /** Current state of an ingest job. {@code progress} is provider-defined and may be null. */
    public record IngestStatus(String status, String progress) {}

    /** Raised for transport failures, rejected credentials and error envelopes. */
    public static class MemCodeException extends RuntimeException {
        public MemCodeException(String message) {
            super(message);
        }

        public MemCodeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
