import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Result;

import java.util.List;

public class StructuredPromptTemplateExamples {

    static String apiKey = System.getenv("OPENAI_API_KEY"); // https://platform.openai.com/account/api-keys
    static ChatLanguageModel model = OpenAiChatModel.withApiKey(apiKey);

    static class Simple_Structured_Prompt_Example {

        @StructuredPrompt("Create a recipe of a {{dish}} that can be prepared using only {{ingredients}}")
        static class CreateRecipePrompt {

            private String dish;
            private List<String> ingredients;
        }

        public static void main(String[] args) {

            CreateRecipePrompt prompt = new CreateRecipePrompt();
            prompt.dish = "salad";
            prompt.ingredients = List.of("cucumber", "tomato", "feta", "onion", "olives");

            Result<AiMessage> result = model.sendUserMessage(prompt);
            System.out.println(result.get().text());
        }
    }

    static class Multi_Line_Structured_Prompt_Example {

        @StructuredPrompt({
                "Create a recipe of a {{dish}} that can be prepared using only {{ingredients}}.",
                "Structure your answer in the following way:",

                "Recipe name: ...",
                "Description: ...",
                "Preparation time: ...",

                "Required ingredients:",
                "- ...",
                "- ...",

                "Instructions:",
                "- ...",
                "- ..."
        })
        static class CreateRecipePrompt {

            private String dish;
            private List<String> ingredients;
        }

        public static void main(String[] args) {

            CreateRecipePrompt prompt = new CreateRecipePrompt();
            prompt.dish = "salad";
            prompt.ingredients = List.of("cucumber", "tomato", "feta", "onion", "olives");

            Result<AiMessage> result = model.sendUserMessage(prompt);
            System.out.println(result.get().text());
        }
    }
}
