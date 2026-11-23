import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

import java.time.LocalDate;

/**
 * Example of tool (function) calling with DeepSeek.
 */
public class DeepSeekToolsExamples {

    static class BookingTools {

        @Tool("Returns the current date")
        LocalDate currentDate() {
            return LocalDate.now();
        }

        @Tool("Books a table for the given number of people on the given date")
        String bookTable(int people, LocalDate date) {
            return "Booked a table for " + people + " people on " + date;
        }
    }

    interface BookingAssistant {

        String chat(String userMessage);
    }

    public static void main(String[] args) {

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY")) // Please use your own DeepSeek API key
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-flash")
                .logRequests(true)
                .logResponses(true)
                .build();

        BookingAssistant assistant = AiServices.builder(BookingAssistant.class)
                .chatModel(model)
                .tools(new BookingTools())
                .build();

        String answer = assistant.chat("Please book a table for 4 people today");

        System.out.println(answer);
    }
}
