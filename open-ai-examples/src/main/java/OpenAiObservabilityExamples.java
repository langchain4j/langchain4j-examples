import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.openai.OpenAiChatModel;

import static java.util.Collections.singletonList;

public class OpenAiObservabilityExamples {

    static class Observe_OpenAiChatModel {

        public static void main(String[] args) {

            ChatModelListener modelListener = new ChatModelListener() {

                @Override
                public void onRequest(ChatModelRequestContext requestContext) {
                    System.out.println("Request: " + requestContext.request().messages());
                }

                @Override
                public void onResponse(ChatModelResponseContext responseContext) {
                    System.out.println("Response: " + responseContext.response().aiMessage());
                }

                @Override
                public void onError(ChatModelErrorContext errorContext) {
                    errorContext.error().printStackTrace();
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
