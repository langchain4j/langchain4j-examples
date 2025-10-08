import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.jlama.JlamaChatModel;

public class JlamaChatModelExamples {

    static class Simple_Prompt {

        public static void main(String[] args) {

            ChatModel model = JlamaChatModel.builder()
                    .modelName("tjake/Llama-3.2-1B-Instruct-JQ4")
                    .temperature(0.3f)
                    .build();

            ChatResponse chatResponse = model.chat(
                    SystemMessage.from("You are helpful chatbot who is a java expert."),
                    UserMessage.from("Write a java program to print hello world.")
            );

            System.out.println("\n" + chatResponse.aiMessage().text() + "\n");
        }
    }
}
