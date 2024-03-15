import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;

import java.io.IOException;

import static dev.langchain4j.data.message.UserMessage.userMessage;

public class ChatMemoryExamples {

    // See also ServiceWithMemoryExample and ServiceWithMemoryForEachUserExample

    public static class ConversationalChain_Example {

        public static void main(String[] args) throws IOException {

            ConversationalChain chain = ConversationalChain.builder()
                    .chatLanguageModel(OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY))
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

            ChatLanguageModel model = OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY);

            ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(300, new OpenAiTokenizer("gpt-3.5-turbo"));

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
}
