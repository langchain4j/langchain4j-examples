# DBpedia Example Project

## Overview

This Java project demonstrates how to interact with the DBpedia SPARQL endpoint to query data and process it using the Azure OpenAI service. The project is structured to perform semantic data extraction and generate meaningful answers based on text queries.

## Project Structure

- **`pom.xml`**: Maven project configuration file that specifies dependencies for Apache Jena and LangChain4j libraries, among others.
- **`Makefile`**: A Makefile to simplify the process of building, running, and cleaning the project using standard commands.
- **`DbPediaSparqlExample.java`**: Main class that handles SPARQL queries against the DBpedia endpoint and processes these queries using Azure's OpenAI model.
- **`AzureOpenAIConfig.java`**: Configuration class to read and manage Azure OpenAI service settings from a JSON file.

## Key Classes

### 1. `DbPediaSparqlExample`
This class is responsible for:
- Creating and executing SPARQL queries to retrieve data from DBpedia.
- Integrating with the Azure OpenAI model to extract subjects from natural language questions.
- Generating answers based on the extracted data and returning them.

### 2. `AzureOpenAIConfig`
This class handles the configuration for the Azure OpenAI service:
- Reads the API key, endpoint, and deployment name from a JSON file.
- Implements the Singleton pattern to ensure only one instance is used throughout the application.

## Usage

### Prerequisites
- **Java 11** or higher.
- **Maven** to handle dependencies and build the project.
- **Make** to use the provided Makefile for building and running the project.
- A valid configuration file for Azure OpenAI service (`key.json`), located in your home directory under `.azureopenapi`.

### Configuration
Ensure that your JSON configuration file (`key.json`) includes the following fields:
```json
{
    "AZURE_OPENAI_KEY": "your-api-key",
    "AZURE_OPENAI_ENDPOINT": "https://your-endpoint.openai.azure.com/",
    "AZURE_OPENAI_DEPLOYMENT_NAME": "your-deployment-name"
}


### Running the Project

You can use the `Makefile` to manage the build and execution of the project easily. The following commands are available:

1. **Build the project**: This compiles the source files and packages them into a JAR file.
   ```bash
   make
   ```

2. **Run the main class `DbPediaSparqlExample`**: This command runs the project, executing the main class.
   ```bash
   make run
   ```

3. **Clean the project**: This command cleans up the project, removing compiled files and the generated JAR.
   ```bash
   make clean
   ```

### Example
When you run the project, it will:
1. Retrieve the subject from a given question (e.g., "How many years did Napoleon live?").
2. Query DBpedia for relevant information (e.g., abstract about Napoleon).
3. Generate a professional answer using the Azure OpenAI model.

## Dependencies
- **Apache Jena**: For interacting with RDF data and executing SPARQL queries.
- **LangChain4j**: For integrating and managing OpenAI models.
- **TinyLog**: For logging purposes.
```

### Summary of Adjustments:
- Added a section for the `Makefile`, describing the build, run, and clean commands.
- Updated the prerequisites to reflect the use of Java 11.
- Simplified the instructions to use `make` commands instead of Maven directly for common tasks.
