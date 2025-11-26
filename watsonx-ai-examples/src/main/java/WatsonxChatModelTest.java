import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.watsonx.WatsonxChatModel;

class WatsonxChatModelTest {

    public static void main(String... args) {

        try {

            ChatModel model = WatsonxChatModel.builder()
                .baseUrl(System.getenv("WATSONX_URL"))
                .apiKey(System.getenv("WATSONX_API_KEY"))
                .projectId(System.getenv("WATSONX_PROJECT_ID"))
                .modelName("ibm/granite-3-8b-instruct")
                .build();

            System.out.println("--------------------------------------------------");
            System.out.println("Granite says: " + model.chat("What is the capital of Italy?"));
            System.out.println("--------------------------------------------------");


            ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from("What is the capital of Italy?"))
                .modelName("mistralai/mistral-small-3-1-24b-instruct-2503")
                .build();

            System.out.println("--------------------------------------------------");
            System.out.println("mistral-small says: " + model.chat(request).aiMessage().text());
            System.out.println("--------------------------------------------------");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
