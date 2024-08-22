import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O;

public class OpenAiChatModelExamples {

    static class Simple_Prompt {

        public static void main(String[] args) {

            ChatLanguageModel model = OpenAiChatModel.withApiKey("demo");

            String joke = model.generate("Tell me a joke about Java");

            System.out.println(joke);
        }
    }

    static class Image_Inputs {

        public static void main(String[] args) {

            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY")) // Please use your own OpenAI API key
                    .modelName(GPT_4_O)
                    .maxTokens(50)
                    .build();

            UserMessage userMessage = UserMessage.from(
                    TextContent.from("What do you see?"),
                    ImageContent.from("https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png")
            );

            Response<AiMessage> response = model.generate(userMessage);

            System.out.println(response.content().text());
        }
    }
}
