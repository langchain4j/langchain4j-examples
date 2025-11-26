import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.watsonx.WatsonxStreamingChatModel;

class WatsonxStreamingChatModelTest {

    public static void main(String... args) throws Exception {

        try {

        StreamingChatModel model = WatsonxStreamingChatModel.builder()
            .baseUrl(System.getenv("WATSONX_URL"))
            .apiKey(System.getenv("WATSONX_API_KEY"))
            .projectId(System.getenv("WATSONX_PROJECT_ID"))
            .modelName("mistralai/mistral-small-3-1-24b-instruct-2503")
            .build();

        CountDownLatch latch = new CountDownLatch(1);
        model.chat("What is the capital of Italy?", new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                latch.countDown();
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
