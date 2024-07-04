import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.jlama.JlamaChatModel;

public class JlamaChatModelExamples {

    static class Simple_Prompt {

        public static void main(String[] args) {

            ChatLanguageModel model = JlamaChatModel.builder()
                    .modelName("tjake/TinyLlama-1.1B-Chat-v1.0-Jlama-Q4")
                    .temperature(0.0f) //Force same output every run
                    .build();

            String joke = model.generate(
                            SystemMessage.from("You are a comedian"),
                            UserMessage.from("Tell me a quick joke about Java"))
                    .content()
                    .text();

            System.out.println("\n" + joke + "\n");
        }
    }
}
