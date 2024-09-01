import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.jlama.JlamaChatModel;

public class JlamaChatModelExamples {

    static class Simple_Prompt {

        public static void main(String[] args) {

            ChatLanguageModel model = JlamaChatModel.builder()
                    .modelName("tjake/TinyLlama-1.1B-Chat-v1.0-Jlama-Q4")
                    .temperature(0.3f)
                    .build();

            String response = model.generate(
                            SystemMessage.from("You are helpful chatbot who is a java expert."),
                            UserMessage.from("Write a java program to print hello world."))
                    .content()
                    .text();

            System.out.println("\n" + response + "\n");
        }
    }
}
