package dev.langchain4j.example.mcp.server.tachyon;

import dev.tachyonmcp.annotations.langchain4j.LangChain4jAnnotationProvider;
import dev.tachyonmcp.core.server.TachyonServer;

public class McpServerExample {

    public static void main(String... args) {
        TachyonServer.builder()
                .host("127.0.0.1")
                .port(8080)
                .info(it -> it.name("my-java-mcp-server")
                        .description("MCP server scanning a LangChain4j @Tool method via annotations")
                        .version("1.0.0"))
                .annotations(a -> a
                        .withProvider(LangChain4jAnnotationProvider.instance())
                        .register(new Calculator()))
                .build()
                .start();
    }
}
