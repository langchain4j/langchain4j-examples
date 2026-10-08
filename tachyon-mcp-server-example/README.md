# MCP GitHub Tools Example

This project demonstrates how to expose [LangChain4j](https://github.com/langchain4j/langchain4j) 
tool over the Streamable HTTP MCP transport. No authentication required.

## Prerequisites

- Java (JDK 21+ recommended)
- Maven
- Nodejs installed for MCP Inspector to run

## Running the Example

Run the `McpServerExample` class. This will:

- Start the Streamable HTTP MCP server listening on http://127.0.0.1:8080/mcp and exposing a simple Calculator tool.

You can run the example from your IDE or with Maven:

```shell
mvn clean package && \
mvn exec:java \
  -Dexec.mainClass=dev.langchain4j.example.mcp.server.tachyon.McpServerExample
```

Please refer to the [MCP Configuration page](https://tachyonmcp.dev/docs/running/configuration/) for advanced MCP server setup.

## Connecting to MCP Server with MCP Inspector

You may use [MCP Inspector](https://github.com/modelcontextprotocol/inspector) to connect and test MCP Server:

```shell
npx -y @modelcontextprotocol/inspector \
  --server-url http://127.0.0.1:8080/mcp \
  --protocol-era auto
```

You may call MCP tool instantly:

```bash
npx -y @modelcontextprotocol/inspector --cli \
  --server-url http://127.0.0.1:8080/mcp \
  --connect-timeout 10000 \
  --protocol-era auto \
  --method tools/call --tool-name add \
  --tool-args-json '{"a":100500,"b":42}' --format json | jq
```

the result will be: 

```json
{
  "result": {
    "content": [
      {
        "type": "text",
        "text": "100542"
      }
    ]
  }
}
```