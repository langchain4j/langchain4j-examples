import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.github.GitHubModelsStreamingChatModel;
import dev.langchain4j.model.output.Response;

import java.util.concurrent.CompletableFuture;

import static dev.langchain4j.model.github.GitHubModelsChatModelName.GPT_4_O_MINI;

public class GitHubModelsStreamingChatModelExamples {

    static class Simple_Streaming_Prompt {

        public static void main(String[] args) {

            GitHubModelsStreamingChatModel model = GitHubModelsStreamingChatModel.builder()
                    .gitHubToken(System.getenv("GITHUB_TOKEN"))
                    .modelName(GPT_4_O_MINI.modelName())
                    .logRequestsAndResponses(true)
                    .build();

            String userMessage = "Write a 100-word poem about Java and AI";

            CompletableFuture<Response<AiMessage>> futureResponse = new CompletableFuture<>();
            model.generate(userMessage, new StreamingResponseHandler<AiMessage>() {

                @Override
                public void onNext(String token) {
                    System.out.print(token);
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
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
