package aca.examples;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.azure.AzureOpenAiChatModel; 
import dev.langchain4j.service.AiServices;
import dev.langchain4j.azure.aca.dynamicsessions.SessionsREPLTool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;


 /**
 * Examples for using a tool for executing code in Azure ACA dynamic sessions.
 * See the examples here for more information:
 * https://github.com/langchain4j/langchain4j/tree/main/langchain4j-azure-aca-dynamic-sessions
 *
 * Overview:
 * This example demonstrates how to leverage Azure ACA dynamic sessions for remote code 
 * execution, file management, and interactive communication using LangChain4j. It 
 * integrates the AzureOpenAiChatModel for conversational capabilities with the 
 * SessionsREPLTool to run code and perform file operations within an Azure Container Apps 
 * dynamic session.
 *
 * Key Components:
 *   - Assistant interface: Provides a chat method for interacting with the language model.
 *   - AzureOpenAiChatModel: Configured to use Azure OpenAI, it handles chat-based interactions.
 *   - SessionsREPLTool: Acts as a tool for executing code remotely in an ACA dynamic session 
 *     and managing files (upload/download/list).
 *   - AiServices: Connects the language model and the tool to form a complete assistant.
 *
 * Required Environment Variables:
 *   POOL_MANAGEMENT_ENDPOINT       - URL for the ACA dynamic sessions pool management.
 *   AZURE_OPENAI_API_KEY           - API key for accessing the Azure OpenAI service.
 *   AZURE_OPENAI_ENDPOINT          - Endpoint URL for the Azure OpenAI service.
 *   AZURE_OPENAI_DEPLOYMENT_NAME   - Deployment name for the Azure OpenAI service.
 *   REGION                         - Azure region (e.g. westus2).
 *   SUBSCRIPTION_ID                - Your Azure subscription ID.
 *   RESOURCE_GROUP                 - Your Azure resource group name.
 *   SESSION_POOL_NAME              - Name of your ACA session pool.
 *   SESSION_POOL_RESOURCE_ID       - Resource ID of your ACA session pool.
 *   CLI_USERNAME                   - Your Azure CLI username.
 *
 * Environment Variable Setup:
 *
 * For Windows (cmd.exe):
 *   set REGION=<your-region>
 *   set SUBSCRIPTION_ID=<your-subscription-id>
 *   set RESOURCE_GROUP=<your-resource-group>
 *   set SESSION_POOL_NAME=<your-session-pool-name>
 *   set POOL_MANAGEMENT_ENDPOINT=<your-pool-management-endpoint>
 *   set AZURE_OPENAI_ENDPOINT=<your-azure-openai-endpoint>
 *   set AZURE_OPENAI_API_KEY=<your-azure-openai-api-key>
 *   set AZURE_OPENAI_DEPLOYMENT_NAME=<your-azure-openai-deployment-name>
 *   set SESSION_POOL_RESOURCE_ID=<your-session-pool-resource-id>
 *   set CLI_USERNAME=<your-cli-username>
 *
 * For Unix/Linux/macOS (bash):
 *   export REGION=<your-region>
 *   export SUBSCRIPTION_ID=<your-subscription-id>
 *   export RESOURCE_GROUP=<your-resource-group>
 *   export SESSION_POOL_NAME=<your-session-pool-name>
 *   export POOL_MANAGEMENT_ENDPOINT=<your-pool-management-endpoint>
 *   export AZURE_OPENAI_ENDPOINT=<your-azure-openai-endpoint>
 *   export AZURE_OPENAI_API_KEY=<your-azure-openai-api-key>
 *   export AZURE_OPENAI_DEPLOYMENT_NAME=<your-azure-openai-deployment-name>
 *   export SESSION_POOL_RESOURCE_ID=<your-session-pool-resource-id>
 *   export CLI_USERNAME=<your-cli-username>
 */

public class AzureACADynamicSessionsExample {

    interface Assistant {
        //Assistant doesn't need @Tool - core langchain4j method for LLM communication
        String chat(String userMessage);
    }


    public static void main(String[] args) {

        // Retrieve the pool management endpoint from the environment variable
        String poolManagementEndpoint = System.getenv("POOL_MANAGEMENT_ENDPOINT");
        if (poolManagementEndpoint == null) {
            System.err.println("Please set the POOL_MANAGEMENT_ENDPOINT environment variable.");
            return;
        }

        // Initialize the SessionsREPLTool
        SessionsREPLTool ReplTool = new SessionsREPLTool(poolManagementEndpoint);

        // Retrieve the Azure OpenAI API key from the environment variable
        String azureApiKey = System.getenv("AZURE_OPENAI_API_KEY");
        if (azureApiKey == null) {
            System.err.println("Please set the AZURE_OPENAI_API_KEY environment variable.");
            return;
        }

        // Retrieve the Azure OpenAI endpoint from the environment variable
        String azureEndpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
        if (azureEndpoint == null) {
            System.err.println("Please set the AZURE_OPENAI_ENDPOINT environment variable.");
            return;
        }

        // Retrieve the Azure OpenAI deployment name from the environment variable
        String deploymentName = System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME");
        if (deploymentName == null) {
            System.err.println("Please set the AZURE_OPENAI_DEPLOYMENT_NAME environment variable.");
            return;
        }

        // Initialize the Azure OpenAI Chat Language Model
        ChatLanguageModel model = AzureOpenAiChatModel.builder()
                .apiKey(azureApiKey)
                .endpoint(azureEndpoint)
                .deploymentName(deploymentName)
                .build();

        // Build the assistant using AiServices, passing the ReplTool directly
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .tools(ReplTool) // Pass the tool instance directly
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        // Get the assistant's response
        
        String question = "If a pizza has a radius 'z' and a depth 'a', what's its volume? (Answer should be in valid Python code)";
        String answer = assistant.chat(question);
        System.out.println("Question: " + question);
        System.out.println("Answer: " + answer);
                
        // Example: Upload a local file
            Path localFilePath = Paths.get("helloworld.java"); // Replace with your local file path

            SessionsREPLTool.FileUploader fileUploader = ReplTool.new MyFileUploader();
            try {
                SessionsREPLTool.RemoteFileMetadata metadata = fileUploader.uploadFileToAca(localFilePath);
                System.out.println("File uploaded successfully from local path. Metadata: " + metadata.getFilename() + ", " + metadata.getSizeInBytes());
            } catch (Exception e) {
                System.err.println("Error uploading file from local path: " + e.getMessage());
            }
        
        // Example: Download a file
            SessionsREPLTool.MyFileDownloader fileDownloader = ReplTool.new MyFileDownloader();
            String fileToDownload = "helloworld.java"; // Replace with the remote file
            try {
                String downloadedFile = fileDownloader.downloadFile(fileToDownload);
                System.out.println("Downloaded File (Base64): " + downloadedFile);
            } catch (Exception e) {
                System.err.println("Error downloading file: " + e.getMessage());
            }


        // Example: List files
            SessionsREPLTool.MyFileLister fileLister = ReplTool.new MyFileLister();
            try {
                String fileList = fileLister.listFiles();
                System.out.println("File List: " + fileList);
            } catch (Exception e) {
                System.err.println("Error listing files: " + e.getMessage());
            }

        // Optional: Force JVM to exit to prevent lingering threads
        System.exit(0);
    }
}
