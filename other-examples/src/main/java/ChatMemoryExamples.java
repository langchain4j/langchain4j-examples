import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.service.AiServices;

import java.io.IOException;
import java.time.Duration;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static java.time.Duration.ofSeconds;

public class ChatMemoryExamples {

    static class AI_Service_with_Memory_Example {

        interface Chat {

            String chat(String userMessage);
        }

        public static void main(String[] args) {

            String apiKey = System.getenv("OPENAI_API_KEY"); // https://platform.openai.com/account/api-keys

            Chat chat = AiServices.builder(Chat.class)
                    .chatLanguageModel(OpenAiChatModel.withApiKey(apiKey))
                    .chatMemory(MessageWindowChatMemory.withCapacity(10))
                    .build();

            String answer = chat.chat("Hello, my name is Klaus");
            System.out.println(answer); // Hello Klaus! How can I assist you today?

            String answerWithName = chat.chat("What is my name?");
            System.out.println(answerWithName); // Your name is Klaus.
        }
    }

    public static class ConversationalChain_Example {

        public static void main(String[] args) throws IOException {

            String apiKey = System.getenv("OPENAI_API_KEY"); // https://platform.openai.com/account/api-keys

            ConversationalChain chain = ConversationalChain.builder()
                    .chatLanguageModel(OpenAiChatModel.withApiKey(apiKey))
                    // .chatMemory() // you can override default chat memory
                    .build();

            String answer = chain.execute("Hello, my name is Klaus");
            System.out.println(answer); // Hello Klaus! How can I assist you today?

            String answerWithName = chain.execute("What is my name?");
            System.out.println(answerWithName); // Your name is Klaus.
        }
    }

    public static class If_You_Need_More_Control {

        public static void main(String[] args) {

            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
                    .modelName(GPT_3_5_TURBO)
                    .temperature(0.3)
                    .timeout(ofSeconds(120))
                    .logRequests(true)
                    .logResponses(true)
                    .build();

            ChatMemory chatMemory = TokenWindowChatMemory.builder()
                    .systemMessage("You are a helpful assistant.")
                    .capacityInTokens(300)
                    .tokenizer(new OpenAiTokenizer(GPT_3_5_TURBO))
                    .build();

            // You have full control over the chat memory.
            // You can decide if you want to add a particular message to the memory
            // (e.g. you might not want to store few-shot examples to save on tokens).
            // You can process/modify the message before saving if required.

            chatMemory.add(userMessage("Hello, my name is Klaus"));
            AiMessage answer = model.sendMessages(chatMemory.messages());
            System.out.println(answer.text()); // Hello Klaus! How can I assist you today?
            chatMemory.add(answer);

            chatMemory.add(userMessage("What is my name?"));
            AiMessage answerWithName = model.sendMessages(chatMemory.messages());
            System.out.println(answerWithName.text()); // Hello Klaus! How can I assist you today?
            chatMemory.add(answerWithName); // Your name is Klaus.
        }
    }
}
