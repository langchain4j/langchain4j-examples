import dev.langchain4j.code.Judge0JavaScriptExecutionTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

public class ServiceWithDynamicToolsExample {

    interface Assistant {

        String chat(String message);
    }

    public static void main(String[] args) {

        String apiKey = System.getenv("OPENAI_API_KEY"); // https://platform.openai.com/account/api-keys
        ChatLanguageModel chatLanguageModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .temperature(0.0)
//                .modelName("gpt-4-0613")
                .build();

        String judge0ApiKey = ""; // TODO where to get?
        Judge0JavaScriptExecutionTool judge0JavaScriptExecutionTool = new Judge0JavaScriptExecutionTool(judge0ApiKey);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withCapacity(10))
                .tools(judge0JavaScriptExecutionTool)
                .build();

        interact(assistant, "What is the square root of 49506838032859?");
        interact(assistant, "Capitalize every third letter in the string 'abcabcabc'");
        interact(assistant, "What is the number of hours between 17:00 on 21 Feb 1988 and 04:00 on 12 Apr 2014?");
    }

    private static void interact(Assistant assistant, String userMessage) {
        System.out.println("[User]: " + userMessage);
        String answer = assistant.chat(userMessage);
        System.out.println("[Assistant]: " + answer);
    }
}
