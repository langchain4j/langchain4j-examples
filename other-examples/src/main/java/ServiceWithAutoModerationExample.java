import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiModerationModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Moderate;
import dev.langchain4j.service.ModerationException;

public class ServiceWithAutoModerationExample {

    interface Chat {

        @Moderate
        String chat(String text);
    }

    public static void main(String[] args) {

        ChatLanguageModel chatLanguageModel = OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY);

        OpenAiModerationModel moderationModel = OpenAiModerationModel.withApiKey(ApiKeys.OPENAI_API_KEY);

        Chat chat = AiServices.builder(Chat.class)
                .chatLanguageModel(chatLanguageModel)
                .moderationModel(moderationModel)
                .build();

        try {
            chat.chat("I WILL KILL YOU!!!");
        } catch (ModerationException e) {
            System.out.println(e.getMessage());
            // Text "I WILL KILL YOU!!!" violates content policy
        }
    }
}
