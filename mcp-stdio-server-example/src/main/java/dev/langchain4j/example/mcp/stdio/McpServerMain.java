package dev.langchain4j.example.mcp.stdio;

import dev.langchain4j.community.mcp.server.McpServer;
import dev.langchain4j.community.mcp.server.transport.StdioMcpServerTransport;
import dev.langchain4j.mcp.protocol.McpImplementation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class McpServerMain {

    public static void main(String[] args) throws Exception {
        McpImplementation serverInfo = new McpImplementation();
        serverInfo.setName("mcp-stdio-server-example");
        serverInfo.setVersion("1.0.0");

        List<Object> tools = List.of(new Calculator());
        McpServer server = new McpServer(tools, serverInfo);
        try (StdioMcpServerTransport transport = new StdioMcpServerTransport(System.in, System.out, server)) {
            awaitTransportClose(transport);
        }
    }

    private static void awaitTransportClose(StdioMcpServerTransport transport) throws Exception {
        try {
            Method awaitClose = StdioMcpServerTransport.class.getMethod("awaitClose");
            awaitClose.invoke(transport);
            return;
        } catch (NoSuchMethodException ignored) {
            // Compatible with released artifacts that predate awaitClose().
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception exception) {
                throw exception;
            }
            throw e;
        }

        Field ioThread = StdioMcpServerTransport.class.getDeclaredField("ioThread");
        ioThread.setAccessible(true);
        ((Thread) ioThread.get(transport)).join();
    }
}
