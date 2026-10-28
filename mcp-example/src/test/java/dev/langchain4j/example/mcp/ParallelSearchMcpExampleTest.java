package dev.langchain4j.example.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.mcp.client.McpApplicationErrorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParallelSearchMcpExampleTest {

    private final ObjectMapper json = new ObjectMapper();
    private final List<JsonNode> requests = new CopyOnWriteArrayList<>();
    private final List<String> userAgents = new CopyOnWriteArrayList<>();
    private final List<String> paths = new CopyOnWriteArrayList<>();
    private final List<String> authorizationHeaders = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private String endpoint;
    private boolean failSearch;

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/mcp", exchange -> {
            JsonNode request = json.readTree(exchange.getRequestBody());
            requests.add(request);
            userAgents.add(exchange.getRequestHeaders().getFirst("User-Agent"));
            paths.add(exchange.getRequestURI().getPath());
            authorizationHeaders.addAll(exchange.getRequestHeaders().getOrDefault("Authorization", List.of()));
            if (!request.has("id")) {
                exchange.sendResponseHeaders(202, -1);
            } else {
                Object result = switch (request.path("method").asText()) {
                    case "initialize" -> Map.of("protocolVersion", "2025-11-25", "capabilities",
                            Map.of("tools", Map.of()), "serverInfo", Map.of("name", "fixture", "version", "1"));
                    case "tools/list" -> Map.of("tools", List.of(
                            Map.of("name", "web_search", "description", "Search the web", "inputSchema",
                                    Map.of("type", "object", "properties", Map.of(
                                            "objective", Map.of("type", "string"),
                                            "search_queries", Map.of("type", "array", "items", Map.of("type", "string"))))),
                            Map.of("name", "web_fetch", "description", "Fetch pages", "inputSchema",
                                    Map.of("type", "object", "properties", Map.of(
                                            "urls", Map.of("type", "array", "items", Map.of("type", "string")),
                                            "objective", Map.of("type", "string"))))));
                    case "tools/call" -> {
                        boolean search = request.path("params").path("name").asText().equals("web_search");
                        boolean error = search && failSearch;
                        yield Map.of("isError", error, "content", List.of(Map.of("type", "text", "text",
                                error ? "Search unavailable" : search
                                        ? "LangChain4j MCP documentation: https://docs.langchain4j.dev/tutorials/mcp/"
                                        : "Configure StreamableHttpMcpTransport and McpToolProvider.")));
                    }
                    default -> throw new IllegalStateException("Unexpected method: " + request);
                };
                byte[] response = json.writeValueAsBytes(Map.of("jsonrpc", "2.0", "id", request.get("id"), "result", result));
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            }
            exchange.close();
        });
        server.start();
        endpoint = "http://127.0.0.1:" + server.getAddress().getPort() + "/mcp";
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void discoversAndExecutesSearchAndFetchWithAnonymousProjectHeaders() throws Exception {
        try (var client = ParallelSearchMcpExample.createClient(endpoint)) {
            assertThat(ParallelSearchMcpExample.runExample(client)).containsExactly(
                    "LangChain4j MCP documentation: https://docs.langchain4j.dev/tutorials/mcp/",
                    "Configure StreamableHttpMcpTransport and McpToolProvider.");
        }
        assertThat(ParallelSearchMcpExample.ENDPOINT).isEqualTo("https://search.parallel.ai/mcp");
        assertThat(paths).isNotEmpty().containsOnly("/mcp");
        assertThat(userAgents).hasSameSizeAs(requests).containsOnly(ParallelSearchMcpExample.USER_AGENT);
        assertThat(authorizationHeaders).isEmpty();
        assertThat(requests.stream().map(r -> r.path("method").asText()))
                .contains("initialize", "notifications/initialized", "tools/list", "tools/call");
        List<JsonNode> calls = requests.stream().filter(r -> r.path("method").asText().equals("tools/call"))
                .map(r -> r.path("params")).toList();
        assertThat(calls).hasSize(2);
        assertThat(calls.get(0).path("name").asText()).isEqualTo("web_search");
        assertThat(calls.get(0).path("arguments").path("search_queries").get(0).asText())
                .isEqualTo("LangChain4j MCP client documentation");
        assertThat(calls.get(1).path("name").asText()).isEqualTo("web_fetch");
        assertThat(calls.get(1).path("arguments").path("urls").get(0).asText())
                .isEqualTo("https://docs.langchain4j.dev/tutorials/mcp/");
    }

    @Test
    void reportsToolErrorsAndStopsBeforeFetching() throws Exception {
        failSearch = true;
        try (var client = ParallelSearchMcpExample.createClient(endpoint)) {
            assertThatThrownBy(() -> ParallelSearchMcpExample.runExample(client))
                    .isInstanceOf(McpApplicationErrorException.class)
                    .hasMessageContaining("Search unavailable");
        }
        assertThat(requests.stream().filter(r -> r.path("method").asText().equals("tools/call"))).hasSize(1);
    }
}
