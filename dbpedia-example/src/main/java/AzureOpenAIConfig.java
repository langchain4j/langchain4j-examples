/**
 * AzureOpenAIConfig.java
 *
 * This class is designed to read and parse a JSON configuration file for the Azure OpenAI service.
 * The configuration file contains essential information such as the API key, the service endpoint,
 * and the deployment name. This class extracts these details and provides static getter methods to
 * access them. The file path is specified at runtime using the user's home directory and an
 * environment variable.
 *
 * This class implements the Singleton pattern to ensure only one instance is created during
 * the lifetime of the application.
 *
 * Example of JSON configuration file ($HOME/.azureopenapi/key.json):
 * {
 *     "AZURE_OPENAI_KEY": "28...96",
 *     "AZURE_OPENAI_ENDPOINT": "https://YOUR-ENDPOINT.openai.azure.com/",
 *     "AZURE_OPENAI_DEPLOYMENT_NAME": "gpt-4o"
 * }
 *
 * Usage:
 * - The file path for the JSON configuration is constructed using the HOME environment variable.
 * - The JSON file is expected to contain keys: AZURE_OPENAI_KEY, AZURE_OPENAI_ENDPOINT, and
 *   AZURE_OPENAI_DEPLOYMENT_NAME.
 * - The extracted values can be accessed through the corresponding static getter methods.
 *
 * Author: Michel Héon
 * Date: 2024-08-22
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;

import java.io.File;
import java.io.IOException;

public class AzureOpenAIConfig {

    private static AzureOpenAIConfig instance;
    private String apiKey;
    private String endpoint;
    private String deploymentName;

    // Private constructor to prevent instantiation from other classes
    private AzureOpenAIConfig(String filePath) throws IOException {
        // Set the logging level to INFO programmatically
        Configuration.set("level", "info");

        // Log an informational message
        Logger.info("Initializing AzureOpenAIConfig with file path: {}", filePath);
        // Read and parse the JSON configuration file
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(new File(filePath));

        // Extract the values of the attributes
        this.apiKey = jsonNode.get("AZURE_OPENAI_KEY").asText();
        this.endpoint = jsonNode.get("AZURE_OPENAI_ENDPOINT").asText();
        this.deploymentName = jsonNode.get("AZURE_OPENAI_DEPLOYMENT_NAME").asText();
    }

    // Method to get the single instance of the class (Singleton pattern)
    public static AzureOpenAIConfig getInstance() throws IOException {
        if (instance == null) {
            // Construct the file path using the HOME environment variable
            String filePath = System.getenv("HOME") + "/.azureopenapi/key.json";
            instance = new AzureOpenAIConfig(filePath);
        }
        return instance;
    }

    // Static getters for each attribute
    public String getApiKey() {
        return apiKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public static void main(String[] args) {
        try {
            // Access the single instance of AzureOpenAIConfig
            AzureOpenAIConfig config = AzureOpenAIConfig.getInstance();

            // Example usage
            System.out.println("API Key: " + config.getApiKey());
            System.out.println("Endpoint: " + config.getEndpoint());
            System.out.println("Deployment Name: " + config.getDeploymentName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
