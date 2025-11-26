import java.time.LocalDate;
import java.time.LocalTime;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.watsonx.WatsonxChatModel;
import dev.langchain4j.service.AiServices;

class WatsonxToolsTest {

    static class Tools {

        @Tool
        LocalDate currentDate() {
            System.out.println("Called currentDate()");
            return LocalDate.now();
        }

        @Tool
        LocalTime currentTime() {
            System.out.println("Called currentTime()");
            return LocalTime.now();
        }
    }

    interface AiService {

        String chat(String userMessage);
    }

    public static void main(String... args) throws Exception {

        try {
            
            ChatModel model = WatsonxChatModel.builder()
                .baseUrl(System.getenv("WATSONX_URL"))
                .apiKey(System.getenv("WATSONX_API_KEY"))
                .projectId(System.getenv("WATSONX_PROJECT_ID"))
                .modelName("meta-llama/llama-3-3-70b-instruct")
                .build();

            AiService aiService = AiServices.builder(AiService.class)
                .chatModel(model)
                .tools(new Tools())
                .build();

            String answer = aiService.chat("What is the date today?");
            System.out.println(answer);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
