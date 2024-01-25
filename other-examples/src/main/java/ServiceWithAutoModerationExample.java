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

    /**
     * 自动审核，是否违反OpenAI的内容政策
     */
    public static void main(String[] args) {

        OpenAiModerationModel moderationModel = OpenAiModerationModel.builder().apiKey(ApiKeys.OPENAI_API_KEY)
                .baseUrl("https://api.baichuan-ai.com/v1").modelName("Baichuan2-Turbo").build();

        Chat chat = AiServices.builder(Chat.class)
                .chatLanguageModel(OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY))
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
