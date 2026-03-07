package dev.langchain4j.example.mcp.stdio;

import dev.langchain4j.community.mcp.server.McpServer;
import dev.langchain4j.community.mcp.server.transport.StdioMcpServerTransport;
import dev.langchain4j.mcp.protocol.McpImplementation;
import java.util.List;

public class McpServerMain {

    public static void main(String[] args) throws Exception {
        McpImplementation serverInfo = new McpImplementation();
        serverInfo.setName("mcp-stdio-server-example");
        serverInfo.setVersion("1.0.0");

        List<Object> tools = List.of(new Calculator());
        McpServer server = new McpServer(tools, serverInfo);
        new StdioMcpServerTransport(System.in, System.out, server);

        // Keep the process alive while stdio is open
        Thread.currentThread().join();
    }
}
