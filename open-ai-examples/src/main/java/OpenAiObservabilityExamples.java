import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.listener.*;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.util.Map;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static java.util.Collections.singletonList;

public class OpenAiObservabilityExamples {

    static class Observe_OpenAiChatModel {

        public static void main(String[] args) {

            ChatModelListener modelListener = new ChatModelListener() {

                @Override
                public void onRequest(ChatModelRequestContext requestContext) {
                    ChatModelRequest request = requestContext.request();
                    Map<Object, Object> attributes = requestContext.attributes();
                }

                @Override
                public void onResponse(ChatModelResponseContext responseContext) {
                    ChatModelResponse response = responseContext.response();
                    ChatModelRequest request = responseContext.request();
                    Map<Object, Object> attributes = responseContext.attributes();
                }

                @Override
                public void onError(ChatModelErrorContext errorContext) {
                    Throwable error = errorContext.error();
                    ChatModelRequest request = errorContext.request();
                    ChatModelResponse partialResponse = errorContext.partialResponse();
                    Map<Object, Object> attributes = errorContext.attributes();
                }
            };

            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey("demo")
                    .modelName(GPT_4_O_MINI)
                    .listeners(singletonList(modelListener))
                    .build();

            model.generate("Tell me a joke about Java");
        }
    }
}
