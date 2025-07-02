# MCP GitHub Tools Example

This project demonstrates how to use the [LangChain4j](https://github.com/langchain4j/langchain4j) framework with the GitHub MCP (Model Context Protocol) server to summarise the latest commits of a public GitHub repository using an LLM (Large Language Model).

## Features

- Connects to the GitHub MCP server via Docker and stdin/stdout.
- Uses OpenAI's GPT-4o-mini model to process and summarise commit data.
- No authentication required for public repositories.

## Prerequisites

- Java (JDK 17+ recommended)
- Maven
- Docker (installed and running)
- OpenAI API key (set as the `OPENAI_API_KEY` environment variable)

## Setup

1. **Build the GitHub MCP Docker image:**

   Follow the instructions at [modelcontextprotocol/servers/src/github](https://github.com/modelcontextprotocol/servers/tree/main/src/git) to build the `mcp/git` Docker image.
```sh
   git clone https://github.com/modelcontextprotocol/servers.git
   cd servers/src/github
   docker build -t mcp/git .
   docker run -i mcp/git
   ```
2. **Set your OpenAI API key:**
```sh
   export OPENAI_API_KEY=your_openai_api_key
   ```
3. **Clone this repository and build the project:** 
```
git clone <this-repo-url> 
cd <this-repo> 
mvn clean package
```

4. ## Running the Example

Run the `McpGithubToolsExample` class. This will:

- Start the GitHub MCP server in a Docker container.
- Use the LLM to summarise the last 3 commits of the [LangChain4j GitHub repository](https://github.com/langchain4j/langchain4j).

You can run the example from your IDE or with Maven:

```mvn exec:java -Dexec.mainClass=dev.langchain4j.example.mcp.github.McpGithubToolsExample```

## Notes

- The MCP server is started as a subprocess using Docker. Make sure Docker is available at `/usr/local/bin/docker` or adjust the path in the code if needed.
- The example does not require a GitHub personal access token for public repositories, but you can provide one via the `GITHUB_PERSONAL_ACCESS_TOKEN` environment variable if needed.

