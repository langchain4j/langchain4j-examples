import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.localai.LocalAiStreamingChatModel;
import dev.langchain4j.model.output.Response;

public class LocalAiStreamingChatModelExamples extends AbstractLocalAiInfrastructure {


    static StreamingChatLanguageModel model = LocalAiStreamingChatModel.builder()
            .baseUrl(localAi.getBaseUrl())
            .modelName("ggml-gpt4all-j")
            .maxTokens(50)
            .logRequests(true)
            .logResponses(true)
            .build();

    static class Simple_Prompt {
        public static void main(String[] args) {

            model.generate("Tell me a poem by Li Bai", new StreamingResponseHandler<AiMessage>() {

                @Override
                public void onNext(String token) {
                    System.out.println("onNext(): " + token);
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    System.out.println("onComplete(): " + response);
                }

                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                }
            });
        }
    }
}
