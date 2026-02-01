# MCP stdio server example

This example exposes a simple calculator tool as an MCP stdio server.

## Build

This example requires LangChain4j and LangChain4j Community versions that include `langchain4j-community-mcp-server`.

Change into this directory, then build the example fat JAR:

```bash
cd mcp-stdio-server-example
mvn -DskipTests package
```

You should see `target/mcp-stdio-server-example-<version>.jar` in this directory.

## Configure MCP client

### Claude Desktop

Add a server entry in `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "my-java-tool": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/mcp-stdio-server-example-<version>.jar"]
    }
  }
}
```

Use absolute paths; on Windows, escape backslashes.

Note: keep `stdout` clean for the JSON-RPC protocol. Configure your logger to write to `stderr`.

### Other MCP clients (Gemini CLI, etc.)

Configure a local stdio MCP server with the same `command` and `args` above.

## Verify

Ask the client:

> "Please calculate 1234 + 5678 using the calculator tool."
