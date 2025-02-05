import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.github.GitHubModelsStreamingChatModel;

import java.util.concurrent.CompletableFuture;

import static dev.langchain4j.model.github.GitHubModelsChatModelName.GPT_4_O_MINI;

public class GitHubModelsStreamingChatModelExamples {

    static class Simple_Streaming_Prompt {

        public static void main(String[] args) {

            GitHubModelsStreamingChatModel model = GitHubModelsStreamingChatModel.builder()
                    .gitHubToken(System.getenv("GITHUB_TOKEN"))
                    .modelName(GPT_4_O_MINI)
                    .logRequestsAndResponses(true)
                    .build();

            String userMessage = "Write a 100-word poem about Java and AI";

            CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

            model.chat(userMessage, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(String partialResponse) {
                    System.out.print(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    futureResponse.complete(completeResponse);
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
