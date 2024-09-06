import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_3_5_TURBO;
import static java.net.Proxy.Type.HTTP;

public class ProxyExample {

    public static void main(String[] args) {

        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(GPT_3_5_TURBO)
                .proxy(new Proxy(HTTP, new InetSocketAddress("39.175.77.7", 30001)))
                .build();

        String answer = model.generate("hello");
        System.out.println(answer);
    }
}
