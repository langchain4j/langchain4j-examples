# MCP stdio server example

This example exposes a simple calculator tool as an MCP stdio server.

## Build

This example requires released versions of LangChain4j and LangChain4j Community that include `langchain4j-community-mcp-server`.
If you are reviewing unreleased changes, use the SNAPSHOT instructions below.

Change into this directory, then build the example fat JAR:

```bash
cd mcp-stdio-server-example
mvn -DskipTests package
```

You should see `target/mcp-stdio-server-example-<version>.jar` in this directory.

### Optional: JavaScript execution tool (dangerous)

This example can optionally expose a JavaScript execution tool, but it is disabled by default.
Only enable code execution in trusted environments.

To include the dependency in the shaded JAR, build with:

```bash
mvn -Pjavascript-tool -DskipTests package
```

Then run the JAR with `--enable-js-tool`.

<details>
<summary>Build against SNAPSHOT builds (development / PR review)</summary>

If you need to test unreleased changes (for example when reviewing PRs),
you can build and install SNAPSHOT artifacts locally and override the versions used by this example.

1. From the `langchain4j` repository root, install the required SNAPSHOT artifacts:

```bash
mvn -pl langchain4j-mcp -am -DskipTests install
```

2. From the `langchain4j-community` repository root, install the MCP server module:

```bash
mvn -pl mcp/langchain4j-community-mcp-server -am -DskipTests install
```

3. Build this example using SNAPSHOT versions:

```bash
mvn "-Dlangchain4j.version=1.11.0-beta19-SNAPSHOT" "-Dlangchain4j-community.version=1.11.0-beta19-SNAPSHOT" -DskipTests package
```

If you also want the optional JavaScript tool, add `-Pjavascript-tool` and run the JAR with `--enable-js-tool`.

</details>

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

If you enabled the optional JavaScript tool:

> "Please calculate 1234 + 5678 using the JavaScript execution tool."

If you need to be explicit about the tool name, it is `executeJavaScriptCode`
and the code must return a result, for example: `return 1234 + 5678;`.
