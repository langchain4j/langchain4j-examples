import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

public class ServiceWithToolsExample {

    // Please also check CustomerSupportApplication and CustomerSupportApplicationTest
    // from spring-boot-example module

    static class Calculator {

        @Tool
        int add(int a, int b) {
            System.out.println("Called add()");
            return a + b;
        }

        @Tool
        double sqrt(int x) {
            System.out.println("Called sqrt()");
            return Math.sqrt(x);
        }
    }

    interface Assistant {

        String chat(String userMessage);
    }

    public static void main(String[] args) {

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY))
                .tools(new Calculator())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        String answer = assistant.chat("What's the square root of 1758395065?");
        System.out.println(answer); // The square root of 1758395065 is approximately 41933.22.
    }
}
