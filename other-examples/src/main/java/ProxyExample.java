import dev.langchain4j.model.openai.OpenAiChatModel;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static java.net.Proxy.Type.HTTP;

public class ProxyExample {

    public static void main(String[] args) {

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .proxy(new Proxy(HTTP, new InetSocketAddress("39.175.77.7", 30001)))
                .build();

        String answer = model.generate("hello");
        System.out.println(answer);
    }
}
