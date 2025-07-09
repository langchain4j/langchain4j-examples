package dev.langchain4j.example.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;

import java.io.File;
import java.util.List;

public class McpToolsExampleOverStdio {

    // We will let the AI read the contents of this file
    public static final String FILE_TO_BE_READ = "src/main/resources/file.txt";

    /**
     * This example uses the `server-filesystem` MCP server to showcase how
     * to allow an LLM to interact with the local filesystem.
     * <p>
     * Running this example requires npm to be installed on your machine,
     * because it spawns the `server-filesystem` as a subprocess via npm:
     * `npm exec @modelcontextprotocol/server-filesystem@0.6.2`.
     * <p>
     * Of course, feel free to swap out the server with any other MCP server.
     * <p>
     * The communication with the server is done directly via stdin/stdout.
     * <p>
     * IMPORTANT: when executing this, make sure that the working directory is
     * equal to the root directory of the project
     * (`langchain4j-examples/mcp-example`), otherwise the program won't be able to find
     * the proper file to read. If you're working from another directory,
     * adjust the path inside the StdioMcpTransport.Builder() usage in the main method.
     */
    public static void main(String[] args) throws Exception {

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o-mini")
//                .logRequests(true)
//                .logResponses(true)
                .build();

        McpTransport transport = new StdioMcpTransport.Builder()
                .command(List.of("/usr/bin/npm", "exec",
                        "@modelcontextprotocol/server-filesystem@0.6.2",
                        // allowed directory for the server to interact with
                        new File("src/main/resources").getAbsolutePath()
                ))
                .logEvents(true)
                .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(List.of(mcpClient))
                .build();

        Bot bot = AiServices.builder(Bot.class)
                .chatModel(model)
                .toolProvider(toolProvider)
                .build();

        try {
            File file = new File(FILE_TO_BE_READ);
            String response = bot.chat("Read the contents of the file " + file.getAbsolutePath());
            System.out.println("RESPONSE: " + response);
        } finally {
            mcpClient.close();
        }
    }
}
