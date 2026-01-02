# MCP stdio server example

This example exposes a simple calculator tool as an MCP stdio server.

## Build (local SNAPSHOT for PRs)

1. From the `langchain4j` repository root, build the MCP artifacts:

```bash
mvn -pl langchain4j-mcp -am -DskipTests install
```

2. From this directory, build the example:

```bash
mvn -DskipTests package
```

## Build (released)

Update `langchain4j.version` in `pom.xml` to a released version (or switch to
the LangChain4j BOM), then run:

```bash
mvn -DskipTests package
```

## Run

```bash
java -jar target/mcp-stdio-server-example-1.11.0-beta19-SNAPSHOT.jar
```

If you changed the version, update the JAR name accordingly.

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
