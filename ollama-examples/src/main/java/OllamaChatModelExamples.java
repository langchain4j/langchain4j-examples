import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;

import java.util.Scanner;

public class OllamaChatModelExamples {

    /**
     * 1. Run Ollama in a Docker: docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama
     * 2. Select a model from the Ollama library: https://ollama.ai/library (choose "phi" if your PC is not high-end)
     * 3. Download and run a model: docker exec -it ollama ollama run mistral
     * 4. Run classes below
     */

    private static final String BASE_URL = "http://localhost:11434";
    private static final String MODEL_NAME = "mistral";

    static class Simple {

        public static void main(String[] args) {

            ChatLanguageModel model = OllamaChatModel.builder()
                    .baseUrl(BASE_URL)
                    .modelName(MODEL_NAME)
                    .build();

            String response = model.generate("Tell me a joke");

            System.out.println(response);
        }
    }

    static class Streaming {

        public static void main(String[] args) {

            StreamingChatLanguageModel model = OllamaStreamingChatModel.builder()
                    .baseUrl(BASE_URL)
                    .modelName(MODEL_NAME)
                    .build();

            model.generate("Tell me a joke", new StreamingResponseHandler<AiMessage>() {

                @Override
                public void onNext(String token) {
                    System.out.print(token);
                }

                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                }
            });
        }
    }

    static class Json_Output {

        public static void main(String[] args) {

            ChatLanguageModel model = OllamaChatModel.builder()
                    .baseUrl(BASE_URL)
                    .modelName(MODEL_NAME)
                    .format("json")
                    .build();

            String response = model.generate("Give me a JSON with 2 fields: name and age of a John Doe, 42");

            System.out.println(response);
        }
    }

    static class Conversation {

        public static void main(String[] args) {

            ChatLanguageModel model = OllamaChatModel.builder()
                    .baseUrl(BASE_URL)
                    .modelName(MODEL_NAME)
                    .build();

            ConversationalChain chain = ConversationalChain.builder()
                    .chatLanguageModel(model)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .build();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("User: ");
                String userMessage = scanner.nextLine();

                if ("exit".equalsIgnoreCase(userMessage)) {
                    break;
                }

                String answer = chain.execute(userMessage);
                System.out.println("AI: " + answer);
            }
        }
    }
}
