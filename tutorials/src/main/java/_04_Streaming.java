import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

import java.util.concurrent.CountDownLatch;

public class _04_Streaming {

    public static void main(String[] args) {

        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(GPT_4_O_MINI)
                .build();

        String prompt = "Write a short funny poem about developers and null-pointers, 10 lines maximum";

        System.out.println("Nr of chars: " + prompt.length());
        System.out.println("Nr of tokens: " + model.estimateTokenCount(prompt));
        
        // Latch to ensure main thread waits
        CountDownLatch latch = new CountDownLatch(1);

        model.chat(prompt, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                System.out.println("\n\nDone streaming");
                latch.countDown();
            }

            @Override
            public void onError(Throwable error) {
                System.out.println("Something went wrong: " + error.getMessage());
                latch.countDown();
            }
        });
        
        try {
            latch.await(); // Wait for the response before proceeding
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Interrupted while waiting for response.");
        }
    }
}
