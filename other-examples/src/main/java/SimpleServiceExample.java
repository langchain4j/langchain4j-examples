import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class SimpleServiceExample {

    interface Assistant {

        String chat(String message);
    }

    public static void main(String[] args) {

        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(GPT_4_O_MINI)
                .build();

        Assistant assistant = AiServices.create(Assistant.class, chatModel);

        String answer = assistant.chat("Hello");

        System.out.println(answer); // Hello! How can I assist you today?
    }
}
