import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class _00_HelloWorld {

    public static void main(String[] args) {

    ChatLanguageModel model = OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY);

    String answer = model.generate("Say Hello World");

    System.out.println(answer);

    }
}
