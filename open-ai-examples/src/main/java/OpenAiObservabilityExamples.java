import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatLanguageModelRequest;
import dev.langchain4j.model.chat.listener.ChatLanguageModelResponse;
import dev.langchain4j.model.listener.ModelListener;
import dev.langchain4j.model.openai.OpenAiChatModel;

import static java.util.Collections.singletonList;

public class OpenAiObservabilityExamples {

    static class Observe_OpenAiChatModel {

        public static void main(String[] args) {

            ModelListener<ChatLanguageModelRequest, ChatLanguageModelResponse> modelListener =
                    new ModelListener<ChatLanguageModelRequest, ChatLanguageModelResponse>() {

                        @Override
                        public void onRequest(ChatLanguageModelRequest request) {
                            System.out.println("Request: " + request.messages());
                        }

                        @Override
                        public void onResponse(ChatLanguageModelResponse response, ChatLanguageModelRequest request) {
                            System.out.println("Response: " + response.aiMessage());
                        }

                        @Override
                        public void onError(Throwable error,
                                            ChatLanguageModelResponse response,
                                            ChatLanguageModelRequest request) {
                            error.printStackTrace();
                        }
                    };

            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey("demo")
                    .listeners(singletonList(modelListener))
                    .build();

            model.generate("Tell me a joke about Java");
        }
    }
}
