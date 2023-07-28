import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingLanguageModel;

import java.util.List;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static java.util.Arrays.asList;

public class StreamingExamples {

    static class StreamingChatLanguageModel_Example {

        public static void main(String[] args) {

            // Sorry, "demo" API key does not support streaming (yet). Please your own key.
            StreamingChatLanguageModel model = OpenAiStreamingChatModel.withApiKey(ApiKeys.OPENAI_API_KEY);

            List<ChatMessage> messages = asList(
                    systemMessage("You are a very sarcastic assistant"),
                    userMessage("Tell me a joke")
            );

            model.sendMessages(messages, new StreamingResponseHandler() {

                @Override
                public void onNext(String token) {
                    System.out.println("New token: '" + token + "'");
                }

                @Override
                public void onComplete() {
                    System.out.println("Streaming completed");
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

            StreamingLanguageModel model = OpenAiStreamingLanguageModel.withApiKey(ApiKeys.OPENAI_API_KEY);

            model.process("Tell me a joke", new StreamingResponseHandler() {

                @Override
                public void onNext(String token) {
                    System.out.println("Next token: '" + token + "'");
                }

                @Override
                public void onComplete() {
                    System.out.println("Streaming completed");
                }

                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                }
            });
        }
    }
}
