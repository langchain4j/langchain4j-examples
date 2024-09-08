import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingLanguageModel;
import dev.langchain4j.model.output.Response;

import java.util.List;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static dev.langchain4j.model.openai.OpenAiLanguageModelName.GPT_3_5_TURBO_INSTRUCT;
import static java.util.Arrays.asList;

public class StreamingExamples {

    static class StreamingChatLanguageModel_Example {

        public static void main(String[] args) {

            // Sorry, "demo" API key does not support streaming. Please use your own key.
            StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .modelName(GPT_4_O_MINI)
                    .build();

            List<ChatMessage> messages = asList(
                    systemMessage("You are a very sarcastic assistant"),
                    userMessage("Tell me a joke")
            );

            model.generate(messages, new StreamingResponseHandler<AiMessage>() {

                @Override
                public void onNext(String token) {
                    System.out.println("New token: '" + token + "'");
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    System.out.println("Streaming completed: " + response);
                }

                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                }
            });
        }
    }

    static class StreamingLanguageModel_Example {

        public static void main(String[] args) {

            // Sorry, "demo" API key does not support streaming. Please use your own key.
            StreamingLanguageModel model = OpenAiStreamingLanguageModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .modelName(GPT_3_5_TURBO_INSTRUCT)
                    .build();

            model.generate("Tell me a joke", new StreamingResponseHandler<String>() {

                @Override
                public void onNext(String token) {
                    System.out.println("New token: '" + token + "'");
                }

                @Override
                public void onComplete(Response<String> response) {
                    System.out.println("Streaming completed: " + response);
                }

                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                }
            });
        }
    }
}
