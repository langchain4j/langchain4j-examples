package dev.langchain4j.example.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class McpToolsExampleOverRemoteHttp {

    /**
     * This example connects to a hosted MCP server over streamable HTTP, so there is nothing to start locally.
     * It uses the deed.page MCP server (https://deed.page/docs), which publishes static files to a public
     * HTTPS URL. The model writes a small HTML page and calls the server's 'publish_site' tool,
     * then answers with the URL of the page.
     * <p>
     * The server needs no account for a first publish. That first publish returns an API token inside the
     * tool result, which means the model sees it. To keep it out of the conversation, set DEEDPAGE_TOKEN
     * to a token you already have: it is sent as an Authorization header on every MCP request instead.
     * <p>
     * Two things worth noticing:
     * - customHeaders(...) on the transport is how you authenticate to a remote MCP server.
     * - filterToolNames(...) exposes only the tools the model needs (the server has many more,
     *   including delete_site and rotate_token).
     * <p>
     * The page is published with a one-hour expiry (ttl_seconds), so the example cleans up after itself.
     */
    public static void main(String[] args) throws Exception {

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o-mini")
                .logRequests(true)
                .logResponses(true)
                .build();

        String token = System.getenv("DEEDPAGE_TOKEN");
        Map<String, String> headers = token == null || token.isBlank()
                ? Map.of()
                : Map.of("Authorization", "Bearer " + token);

        McpTransport transport = new StreamableHttpMcpTransport.Builder()
                .url("https://deed.page/mcp")
                .customHeaders(headers)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(false) // the response of a first publish contains the new token
                .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(List.of(mcpClient))
                .filterToolNames("publish_site")
                .build();

        Bot bot = AiServices.builder(Bot.class)
                .chatModel(model)
                .toolProvider(toolProvider)
                .build();

        String slug = "langchain4j-mcp-" + UUID.randomUUID().toString().substring(0, 8);
        try {
            String response = bot.chat("Write a small, self-contained HTML page (inline CSS only) with a haiku " +
                    "about Java. Publish it as index.html using the slug '" + slug + "' and ttl_seconds 3600. " +
                    "Then reply with the URL of the page and nothing else.");
            System.out.println(response);
        } finally {
            mcpClient.close();
        }
    }
}
