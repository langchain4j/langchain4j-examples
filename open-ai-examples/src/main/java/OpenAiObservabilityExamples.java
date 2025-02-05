import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiChatResponseMetadata;
import dev.langchain4j.model.openai.OpenAiTokenUsage;
import dev.langchain4j.model.output.TokenUsage;

import java.util.List;
import java.util.Map;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class OpenAiObservabilityExamples {

    static class Observe_OpenAiChatModel {

        public static void main(String[] args) {

            ChatModelListener listener = new ChatModelListener() {

                @Override
                public void onRequest(ChatModelRequestContext requestContext) {
                    ChatRequest chatRequest = requestContext.chatRequest();

                    List<ChatMessage> messages = chatRequest.messages();
                    System.out.println(messages);

                    ChatRequestParameters parameters = chatRequest.parameters();
                    System.out.println(parameters.modelName());
                    System.out.println(parameters.temperature());
                    System.out.println(parameters.topP());
                    System.out.println(parameters.topK());
                    System.out.println(parameters.frequencyPenalty());
                    System.out.println(parameters.presencePenalty());
                    System.out.println(parameters.maxOutputTokens());
                    System.out.println(parameters.stopSequences());
                    System.out.println(parameters.toolSpecifications());
                    System.out.println(parameters.toolChoice());
                    System.out.println(parameters.responseFormat());

                    if (parameters instanceof OpenAiChatRequestParameters openAiParameters) {
                        System.out.println(openAiParameters.maxCompletionTokens());
                        System.out.println(openAiParameters.logitBias());
                        System.out.println(openAiParameters.parallelToolCalls());
                        System.out.println(openAiParameters.seed());
                        System.out.println(openAiParameters.user());
                        System.out.println(openAiParameters.store());
                        System.out.println(openAiParameters.metadata());
                        System.out.println(openAiParameters.serviceTier());
                        System.out.println(openAiParameters.reasoningEffort());
                    }

                    Map<Object, Object> attributes = requestContext.attributes();
                    attributes.put("my-attribute", "my-value");
                }

                @Override
                public void onResponse(ChatModelResponseContext responseContext) {
                    ChatResponse chatResponse = responseContext.chatResponse();

                    AiMessage aiMessage = chatResponse.aiMessage();
                    System.out.println(aiMessage);

                    ChatResponseMetadata metadata = chatResponse.metadata();
                    System.out.println(metadata.id());
                    System.out.println(metadata.modelName());
                    System.out.println(metadata.finishReason());

                    if (metadata instanceof OpenAiChatResponseMetadata openAiMetadata) {
                        System.out.println(openAiMetadata.created());
                        System.out.println(openAiMetadata.serviceTier());
                        System.out.println(openAiMetadata.systemFingerprint());
                    }

                    TokenUsage tokenUsage = metadata.tokenUsage();
                    System.out.println(tokenUsage.inputTokenCount());
                    System.out.println(tokenUsage.outputTokenCount());
                    System.out.println(tokenUsage.totalTokenCount());
                    if (tokenUsage instanceof OpenAiTokenUsage openAiTokenUsage) {
                        System.out.println(openAiTokenUsage.inputTokensDetails().cachedTokens());
                        System.out.println(openAiTokenUsage.outputTokensDetails().reasoningTokens());
                    }

                    ChatRequest chatRequest = responseContext.chatRequest();
                    System.out.println(chatRequest);

                    Map<Object, Object> attributes = responseContext.attributes();
                    System.out.println(attributes.get("my-attribute"));
                }

                @Override
                public void onError(ChatModelErrorContext errorContext) {
                    Throwable error = errorContext.error();
                    error.printStackTrace();

                    ChatRequest chatRequest = errorContext.chatRequest();
                    System.out.println(chatRequest);

                    Map<Object, Object> attributes = errorContext.attributes();
                    System.out.println(attributes.get("my-attribute"));
                }
            };

            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName(GPT_4_O_MINI)
                    .listeners(List.of(listener))
                    .build();

            model.chat("Tell me a joke about Java");
        }
    }
}
