# MCP stdio server example

This example exposes a simple calculator tool and a JavaScript execution tool
as an MCP stdio server. Only enable code execution in trusted environments.

## Build (local SNAPSHOT for PRs)

1. From the `langchain4j` repository root, build the MCP artifacts:

```bash
mvn -pl langchain4j-mcp -am -DskipTests install
mvn -pl code-execution-engines/langchain4j-code-execution-engine-graalvm-polyglot -am -DskipTests install
```

Run these as two separate commands. In PowerShell you can also do:
`mvn -pl langchain4j-mcp -am -DskipTests install; mvn -pl code-execution-engines/langchain4j-code-execution-engine-graalvm-polyglot -am -DskipTests install`

2. From this directory, build the example:

```bash
mvn -DskipTests package
```

## Configure MCP client

### Claude Desktop

Add a server entry in `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "my-java-tool": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/mcp-stdio-server-example-1.11.0-beta19-SNAPSHOT.jar"]
    }
  }
}
```

Use absolute paths; on Windows, escape backslashes.

### Other MCP clients (Gemini CLI, etc.)

Configure a local stdio MCP server with the same `command` and `args` above.

## Verify

Ask the client:

> "Please calculate 1234 + 5678 using the calculator tool."

If you want to test the JavaScript tool:

> "Please calculate 1234 + 5678 using the JavaScript execution tool."

If you need to be explicit about the tool name, it is `executeJavaScriptCode`
and the code must return a result, for example: `return 1234 + 5678;`.
