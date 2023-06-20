import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;

public class ChatMemoryExamples {

    public static class IfYouNeedSimplicity {

        public static void main(String[] args) throws IOException {

            ConversationalChain chain = ConversationalChain.builder()
                    .chatLanguageModel(OpenAiChatModel.builder()
                            .modelName(GPT_3_5_TURBO)
                            .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
                            .build())
                    .chatMemory(MessageWindowChatMemory.builder()
                            .systemMessage("You are a helpful assistant.")
                            .capacityInMessages(7)
                            .build())
                    .build();

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("User: ");
                String messageFromUser = br.readLine();

                if ("exit".equalsIgnoreCase(messageFromUser)) {
                    return;
                }

                String responseFromAi = chain.execute(messageFromUser);
                System.out.println("AI: " + responseFromAi);
            }
        }
    }

    public static class IfYouNeedMoreControl {

        public static void main(String[] args) {

            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
                    .modelName(GPT_3_5_TURBO)
                    .temperature(0.3)
                    .timeout(Duration.ofSeconds(120))
                    .logRequests(true)
                    .logResponses(true)
                    .build();

            ChatMemory chatMemory = TokenWindowChatMemory.builder()
                    .systemMessage("You are a helpful assistant.")
                    .capacityInTokens(300)
                    .tokenizer(new OpenAiTokenizer(GPT_3_5_TURBO))
                    .build();

            UserMessage userMessage = userMessage("Tell me a joke");

            // You have full control over the chat memory.
            // You can decide if you want to add a particular message to the memory
            // (e.g. you might not want to store few-shot examples to save on tokens).
            // You can process/modify the message before saving if required.
            chatMemory.add(userMessage);

            AiMessage aiMessage = model.sendMessages(chatMemory.messages()).get();

            chatMemory.add(aiMessage);

            System.out.println(aiMessage.text());
        }
    }
}
