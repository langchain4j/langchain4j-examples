import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;

import static dev.langchain4j.data.message.UserMessage.userMessage;

public class ChatMemoryExamples {

    /**
     * This example demonstrates how to use a low-level {@link ChatMemory} API.
     * For a high-level API with AI Services see {@link ServiceWithMemoryExample}.
     */

    public static void main(String[] args) {

        ChatLanguageModel model = OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY);

        ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(300, new OpenAiTokenizer());

        // You have full control over the chat memory.
        // You can decide if you want to add a particular message to the memory
        // (e.g. you might not want to store few-shot examples to save on tokens).
        // You can process/modify the message before saving if required.

        chatMemory.add(userMessage("Hello, my name is Klaus"));
        AiMessage answer = model.generate(chatMemory.messages()).content();
        System.out.println(answer.text()); // Hello Klaus! How can I assist you today?
        chatMemory.add(answer);

        chatMemory.add(userMessage("What is my name?"));
        AiMessage answerWithName = model.generate(chatMemory.messages()).content();
        System.out.println(answerWithName.text()); // Your name is Klaus.
        chatMemory.add(answerWithName);
    }
}
