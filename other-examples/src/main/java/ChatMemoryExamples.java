import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;

import java.io.IOException;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;

public class ChatMemoryExamples {

    // See also ServiceWithMemoryExample and ServiceWithMemoryForEachUserExample

    public static class ConversationalChain_Example {

        public static void main(String[] args) throws IOException {

            ChatLanguageModel chatLanguageModel = OpenAiChatModel.builder().apiKey(ApiKeys.OPENAI_API_KEY)
                    .baseUrl("https://api.baichuan-ai.com/v1").modelName("Baichuan2-Turbo").build();

            ConversationalChain chain = ConversationalChain.builder()
                    .chatLanguageModel(chatLanguageModel)
                    // .chatMemory() // you can override default chat memory
                    .build();

            String answer = chain.execute("你好，我是李明");
            System.out.println(answer); // Hello Klaus! How can I assist you today?

            String answerWithName = chain.execute("我的名字是什么");
            System.out.println(answerWithName); // Your name is Klaus.
        }
    }

    public static class If_You_Need_More_Control {

        public static void main(String[] args) {

            ChatLanguageModel model = OpenAiChatModel.builder().apiKey(ApiKeys.OPENAI_API_KEY)
                    .baseUrl("https://api.baichuan-ai.com/v1").modelName("Baichuan2-Turbo").build();

            ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(300, new OpenAiTokenizer(GPT_3_5_TURBO));

            // You have full control over the chat memory.
            // You can decide if you want to add a particular message to the memory
            // (e.g. you might not want to store few-shot examples to save on tokens).
            // You can process/modify the message before saving if required.

            chatMemory.add(userMessage("你好, 我是李明"));
            AiMessage answer = model.generate(chatMemory.messages()).content();
            System.out.println(answer.text()); // Hello Klaus! How can I assist you today?
            chatMemory.add(answer);

            chatMemory.add(userMessage("我的名字是什么"));
            AiMessage answerWithName = model.generate(chatMemory.messages()).content();
            System.out.println(answerWithName.text()); // Your name is Klaus.
            chatMemory.add(answerWithName);
        }
    }
}
