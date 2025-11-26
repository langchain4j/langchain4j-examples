import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import com.ibm.watsonx.ai.chat.model.ExtractionTags;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.watsonx.WatsonxStreamingChatModel;

class WatsonxStreamingChatModelReasoningTest {

    public static void main(String... args) throws Exception {

        try {

            StreamingChatModel model = WatsonxStreamingChatModel.builder()
                .baseUrl(System.getenv("WATSONX_URL"))
                .apiKey(System.getenv("WATSONX_API_KEY"))
                .projectId(System.getenv("WATSONX_PROJECT_ID"))
                .modelName("ibm/granite-3-3-8b-instruct")
                .timeout(Duration.ofSeconds(30))
                .thinking(ExtractionTags.of("think", "response"))
                .build();

            CountDownLatch latch = new CountDownLatch(1);

            List<ChatMessage> messages = List.of(
                UserMessage.userMessage("Why the sky is blue?")
            );

            ChatRequest chatRequest = ChatRequest.builder()
                .messages(messages)
                .maxOutputTokens(0)
                .build();

            model.chat(chatRequest, new StreamingChatResponseHandler() {
                boolean firstThinkingChunk = true;
                boolean firstResponseChunk = true;

                @Override
                public void onPartialResponse(String partialResponse) {
                    if (firstResponseChunk) {
                        firstResponseChunk = false;
                        System.out.println("""

                            -----------------------------------
                            |            RESPONSE             |
                            -----------------------------------""");
                    }
                    System.out.print(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    latch.countDown();
                }

                @Override
                public void onPartialThinking(PartialThinking partialThinking) {
                    if (firstThinkingChunk) {
                        firstThinkingChunk = false;
                        System.out.println("""
                            -----------------------------------
                            |            THINKING             |
                            -----------------------------------""");
                    }
                    System.out.print(partialThinking.text());
                }

                @Override
                public void onError(Throwable error) {
                    System.err.println(error);
                }
            });

            latch.await(20, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
