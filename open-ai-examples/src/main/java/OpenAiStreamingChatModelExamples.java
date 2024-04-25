import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;

public class OpenAiStreamingChatModelExamples {

    static class Simple_Prompt {

        public static void main(String[] args) {

            StreamingChatLanguageModel model = OpenAiStreamingChatModel.withApiKey(System.getenv("OPENAI_API_KEY"));

            model.generate("Tell me a joke about Java", new StreamingResponseHandler<AiMessage>() {

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
