import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingLanguageModel;
import dev.langchain4j.model.output.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static dev.langchain4j.model.openai.OpenAiLanguageModelName.GPT_3_5_TURBO_INSTRUCT;
import static java.util.Arrays.asList;

public class StreamingExamples {

    static class StreamingChatModel_Example {

        public static void main(String[] args) {

            // Sorry, "demo" API key does not support streaming. Please use your own key.
            StreamingChatModel model = OpenAiStreamingChatModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .modelName(GPT_4_O_MINI)
                    .build();

            List<ChatMessage> messages = asList(
                    systemMessage("You are a very sarcastic assistant"),
                    userMessage("Tell me a joke")
            );

            CompletableFuture<ChatResponse> futureChatResponse = new CompletableFuture<>();

            model.chat(messages, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(String partialResponse) {
                    System.out.print(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    futureChatResponse.complete(completeResponse);
                }

                @Override
                public void onError(Throwable error) {
                    futureChatResponse.completeExceptionally(error);
                }
            });

            futureChatResponse.join();
        }
    }

    static class StreamingLanguageModel_Example {

        public static void main(String[] args) {

            // Sorry, "demo" API key does not support streaming. Please use your own key.
            StreamingLanguageModel model = OpenAiStreamingLanguageModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .modelName(GPT_3_5_TURBO_INSTRUCT)
                    .build();

            CompletableFuture<Response<String>> futureResponse = new CompletableFuture<>();

            model.generate("Tell me a joke", new StreamingResponseHandler<>() {

                @Override
                public void onNext(String token) {
                    System.out.print(token);
                }

                @Override
                public void onComplete(Response<String> response) {
                    futureResponse.complete(response);
                }

                @Override
                public void onError(Throwable error) {
                    futureResponse.completeExceptionally(error);
                }
            });

            futureResponse.join();
        }
    }
}
