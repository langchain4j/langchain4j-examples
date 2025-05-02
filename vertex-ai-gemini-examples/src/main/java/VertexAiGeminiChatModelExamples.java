import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiStreamingChatModel;

import java.util.concurrent.CompletableFuture;

public class VertexAiGeminiChatModelExamples {

    /**
     * 1. Enable Vertex AI in Google Cloud Console
     * 2. Set your own project and location below
     */
    private static final String PROJECT = "langchain4j";
    private static final String LOCATION = "us-central1";
    private static final String MODEL_NAME = "gemini-pro";

    static class Simple {

        public static void main(String[] args) {

            ChatModel model = VertexAiGeminiChatModel.builder()
                    .project(PROJECT)
                    .location(LOCATION)
                    .modelName(MODEL_NAME)
                    .build();

            String response = model.chat("Tell me a joke");

            System.out.println(response);
        }
    }

    static class Streaming {

        public static void main(String[] args) {

            StreamingChatModel model = VertexAiGeminiStreamingChatModel.builder()
                    .project(PROJECT)
                    .location(LOCATION)
                    .modelName(MODEL_NAME)
                    .build();

            CompletableFuture<ChatResponse> futureChatResponse = new CompletableFuture<>();

            model.chat("Tell me a long joke", new StreamingChatResponseHandler() {

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
}
