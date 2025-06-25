package agent;


import dev.langchain4j.community.store.embedding.neo4j.Neo4jEmbeddingStore;
import dev.langchain4j.community.store.memory.chat.neo4j.Neo4jChatMemoryStore;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.testcontainers.containers.Neo4jContainer;
import util.Utils;

import java.util.Scanner;
import java.util.UUID;

import static agent.CustomerUtil.createAssistant;
import static agent.CustomerUtil.createEmbeddingStore;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;


/**
 * example prompt: `What is the cancellation policy?`
 */
public class CustomerSupportAgentNeo4jApplicationWithoutSpringBoot {

    public static void main(String[] args) {
        try (Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.26.6-enterprise").withLabsPlugins("apoc").withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT", "yes").withAdminPassword("pass1234")) {
            neo4j.start();

            // Setup
            String openAiApiKey = System.getenv("OPENAI_API_KEY");
            String baseUrl = System.getenv("OPENAI_BASE_URL");

            ChatModel chatModel = OpenAiChatModel.builder()
                    .apiKey(openAiApiKey)
                    .baseUrl(baseUrl)
                    .modelName(GPT_4_O_MINI)
                    .build();

            AllMiniLmL6V2QuantizedEmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

            Neo4jChatMemoryStore chatMemoryStore = Neo4jChatMemoryStore.builder()
                    .withBasicAuth(neo4j.getBoltUrl(), "neo4j", neo4j.getAdminPassword())
                    .build();

            Neo4jEmbeddingStore embeddingStore = createEmbeddingStore(neo4j, embeddingModel);


            Utils.Assistant assistant = createAssistant(chatModel, chatMemoryStore);

            CustomerUtil.AssistantService service = new CustomerUtil.AssistantService(assistant, embeddingStore, embeddingModel);

            // Interactive loop
            try (Scanner scanner = new Scanner(System.in)) {
                String sessionId = UUID.randomUUID().toString();

                System.out.println("Welcome to the Customer Support Assistant!");
                System.out.println("Type 'exit' to quit.");

                while (true) {
                    System.out.print("\nYou: ");
                    String input = scanner.nextLine();

                    if ("exit".equalsIgnoreCase(input)) {
                        break;
                    }

                    String response = service.chat(sessionId, input);
                    System.out.println("Assistant: " + response);
                }
            }
        }
    }
}