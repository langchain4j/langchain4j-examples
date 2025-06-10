import static dev.langchain4j.model.LambdaStreamingResponseHandler.onPartialResponseAndErrorBlocking;
import static dev.langchain4j.model.LambdaStreamingResponseHandler.onPartialResponseBlocking;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;


public class LambdaStreamingExamples {

    static class LambdaChatModelExample {

        public static void main(String[] args) throws InterruptedException {
            // Note: "demo" key does not support streaming, please use your own key.
            StreamingChatModel model = OpenAiStreamingChatModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .modelName(OpenAiChatModelName.GPT_4_1_NANO)
                    .build();

            // Example of streaming a response with partial updates
            onPartialResponseBlocking(model, "Why is the sky blue?", System.out::print);

            // Example of streaming a response with error handling but no error expected
            onPartialResponseAndErrorBlocking(
                    model,
                    "Explain quantum physics",
                    System.out::print,
                    error -> System.err.println("Error: " + error.getMessage()));
        }
    }

    static class LambdaChatModelErrorExample {

        public static void main(String[] args) throws InterruptedException {
            StreamingChatModel invalidModel = OpenAiStreamingChatModel.builder()
                    .apiKey("demo")
                    .modelName(OpenAiChatModelName.GPT_4_1_NANO)
                    .build();

            onPartialResponseAndErrorBlocking(
                    invalidModel,
                    "'demo' key does not support streaming",
                    System.out::print,
                    error -> System.err.println("Error caught: " + error.getMessage()));
            System.out.println("\nError handling test completed!");
        }
    }
}
