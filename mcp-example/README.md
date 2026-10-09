# MCP examples

## Parallel web search and fetch

`ParallelSearchMcpExample` connects to [Parallel Search MCP](https://docs.parallel.ai/integrations/mcp/search-mcp)
using LangChain4j's Streamable HTTP transport. It discovers tools through `McpToolProvider`,
searches for the LangChain4j MCP documentation, fetches that documentation page, and prints
the results. It calls the discovered tool executors directly, so no chat model or API key
is needed.

With Java 17 or newer and Maven installed, run from the repository root:

```shell
mvn -f mcp-example/pom.xml compile org.codehaus.mojo:exec-maven-plugin:3.6.3:java \
  -Dexec.mainClass=dev.langchain4j.example.mcp.ParallelSearchMcpExample
```

Anonymous access to `https://search.parallel.ai/mcp` is free at lower rate limits and
intended for exploration and light use. The example sends no authorization header.
Edit the tool arguments in `runExample` to try a different search or page.
Tool failures are reported as errors rather than printed as successful results.

The existing `McpToolsExampleOverHttp` and `McpToolsExampleOverStdio` examples demonstrate
chat model tool use with a local MCP server.

Run the offline transport tests with:

```shell
mvn -f mcp-example/pom.xml test
```
