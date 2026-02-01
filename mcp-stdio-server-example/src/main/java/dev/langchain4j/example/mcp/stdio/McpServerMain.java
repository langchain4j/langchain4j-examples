package dev.langchain4j.example.mcp.stdio;

import dev.langchain4j.community.mcp.server.McpServer;
import dev.langchain4j.community.mcp.server.transport.StdioMcpServerTransport;
import dev.langchain4j.mcp.protocol.McpImplementation;
import java.util.ArrayList;
import java.util.List;

public class McpServerMain {

    public static void main(String[] args) throws Exception {
        McpImplementation serverInfo = new McpImplementation();
        serverInfo.setName("mcp-stdio-server-example");
        serverInfo.setVersion("1.0.0");

        List<Object> tools = new ArrayList<>();
        tools.add(new Calculator());

        if (isJavaScriptToolEnabled(args)) {
            Object jsTool = tryCreateJavaScriptTool();
            if (jsTool != null) {
                tools.add(jsTool);
            } else {
                System.err.println(
                        "JavaScript tool is enabled, but the dependency is not on the classpath. "
                                + "Build with '-Pjavascript-tool' and run again.");
            }
        }

        McpServer server = new McpServer(List.copyOf(tools), serverInfo);
        new StdioMcpServerTransport(System.in, System.out, server);

        // Keep the process alive while stdio is open
        Thread.currentThread().join();
    }

    private static boolean isJavaScriptToolEnabled(String[] args) {
        for (String arg : args) {
            if ("--enable-js-tool".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static Object tryCreateJavaScriptTool() {
        try {
            Class<?> toolClass =
                    Class.forName("dev.langchain4j.agent.tool.graalvm.GraalVmJavaScriptExecutionTool");
            return toolClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JavaScript tool", e);
        }
    }
}
