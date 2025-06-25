package agent;


import dev.langchain4j.community.store.embedding.neo4j.Neo4jEmbeddingStore;
import dev.langchain4j.community.store.memory.chat.neo4j.Neo4jChatMemoryStore;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.Neo4jContainer;
import util.Utils;

import java.util.Scanner;

import static agent.CustomerUtil.createAssistant;
import static agent.CustomerUtil.createEmbeddingStore;

/**
 * example prompt: `What is the cancellation policy?`
 */
@SpringBootApplication
public class CustomerSupportAgentNeo4jWithSpringBoot {

    private static final Logger log = LoggerFactory.getLogger(CustomerSupportAgentNeo4jWithSpringBoot.class);
    private static Neo4jContainer container = new Neo4jContainer<>("neo4j:5.26.6-enterprise").withLabsPlugins("apoc").withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT", "yes").withAdminPassword("pass1234");

    public static void main(String[] args) {
        container.start();
        SpringApplication.run(CustomerSupportAgentNeo4jWithSpringBoot.class, args);
        container.stop();
    }

    @Configuration
    public static class CustomerSupportAgentConfiguration {

        @Bean
        public Neo4jEmbeddingStore embeddingStore() {
            return createEmbeddingStore(container, embeddingModel());
        }

        @Bean
        public Neo4jChatMemoryStore chatMemoryStore() {
            return Neo4jChatMemoryStore.builder()
                    .withBasicAuth(container.getBoltUrl(), "neo4j", container.getAdminPassword())
                    .build();
        }

        @Bean
        public EmbeddingModel embeddingModel() {
            return new AllMiniLmL6V2QuantizedEmbeddingModel();
        }

        @Bean
        public ChatModel chatModel() {
            return OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .baseUrl(System.getenv("OPENAI_BASE_URL"))
                    .build();
        }

        @Bean
        public Utils.Assistant assistant(ChatModel chatLanguageModel, Neo4jChatMemoryStore chatMemoryStore) {
            return createAssistant(chatLanguageModel, chatMemoryStore);
        }

        @Bean
        public CustomerUtil.AssistantService assistantService(Utils.Assistant assistant, Neo4jEmbeddingStore embeddingStore,
                                                              EmbeddingModel embeddingModel) {
            return new CustomerUtil.AssistantService(assistant, embeddingStore, embeddingModel);
        }

        @Bean
        public ApplicationRunner runner(CustomerUtil.AssistantService assistantService) {
            return args -> {
                try (Scanner scanner = new Scanner(System.in)) {
                    String sessionId = "user-123";
                    while (true) {
                        log.info("==================================================");
                        log.info("User: ");
                        String userQuery = scanner.nextLine();
                        if ("exit".equalsIgnoreCase(userQuery)) break;
                        log.info("==================================================");
                        String response = assistantService.chat(sessionId, userQuery);
                        log.info("==================================================");
                        log.info("Assistant: " + response);
                    }
                }
            };
        }
    }
}