import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;

public class ServiceWithStreamingExample {

    interface Assistant {

        TokenStream chat(String message);
    }

    public static void main(String[] args) {

        // Sorry, "demo" API key does not support streaming (yet). Please your own key.
        StreamingChatLanguageModel model = OpenAiStreamingChatModel.withApiKey(ApiKeys.OPENAI_API_KEY);

        Assistant assistant = AiServices.create(Assistant.class, model);

        assistant.chat("Tell me a joke")
                .onNext(System.out::println)
                .onComplete(() -> System.out.println("Streaming completed"))
                .onError(Throwable::printStackTrace)
                .start();
    }
}
