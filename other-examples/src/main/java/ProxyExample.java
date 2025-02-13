import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class ProxyExample {

    public static void main(String[] args) {

        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress("39.175.77.7", 30001)));

        OpenAiChatModel model = OpenAiChatModel.builder()
                .httpClientBuilder(JdkHttpClient.builder().httpClientBuilder(httpClientBuilder))
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(GPT_4_O_MINI)
                .build();

        String answer = model.chat("hello");
        System.out.println(answer);
    }
}
