import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class _00_HelloDevoxx {

    public static void main(String[] args) {

        ChatLanguageModel model = OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY);

        String response = model.generate("Say Hello Devoxx");

        System.out.println(response);
    }
}
