package dev.langchain4j.example.mcp;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Discovers and executes Parallel's web search and page fetch tools without an API key or a chat model.
 * See https://docs.parallel.ai/integrations/mcp/search-mcp for the anonymous service's limits.
 */
public class ParallelSearchMcpExample {

    static final String ENDPOINT = "https://search.parallel.ai/mcp";
    static final String USER_AGENT = "langchain4j-examples/ParallelSearchMcpExample";

    public static void main(String[] args) throws Exception {
        try (McpClient client = createClient(ENDPOINT)) {
            List<String> results = runExample(client);
            System.out.println("Search results:\n" + results.get(0));
            System.out.println("Fetched page:\n" + results.get(1));
        }
    }

    static McpClient createClient(String endpoint) {
        return new DefaultMcpClient.Builder()
                .transport(new StreamableHttpMcpTransport.Builder()
                        .url(endpoint)
                        .customHeaders(Map.of("User-Agent", USER_AGENT))
                        .timeout(Duration.ofSeconds(60))
                        .build())
                .toolExecutionTimeout(Duration.ofSeconds(60))
                .build();
    }

    static List<String> runExample(McpClient client) {
        McpToolProvider provider = McpToolProvider.builder()
                .mcpClients(client)
                .failIfOneServerFails(true)
                .build();
        ToolProviderResult tools = provider.provideTools(
                new ToolProviderRequest(null, UserMessage.from("Find the LangChain4j MCP documentation")));

        String search = execute(tools, "web_search", """
                {"objective":"Find the LangChain4j MCP client documentation",
                 "search_queries":["LangChain4j MCP client documentation"]}
                """);
        String fetch = execute(tools, "web_fetch", """
                {"urls":["https://docs.langchain4j.dev/tutorials/mcp/"],
                 "objective":"How to configure a LangChain4j MCP client"}
                """);
        return List.of(search, fetch);
    }

    private static String execute(ToolProviderResult tools, String name, String arguments) {
        var tool = tools.aiServiceTools().stream()
                .filter(candidate -> candidate.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("MCP server did not advertise " + name));
        return tool.toolExecutor().executeWithContext(
                ToolExecutionRequest.builder().name(name).arguments(arguments).build(), null).resultText();
    }
}
