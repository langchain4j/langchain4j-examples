import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.util.List;

import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;

public class StructuredPromptTemplateExamples {

    static ChatLanguageModel model = OpenAiChatModel.builder()
            .apiKey(ApiKeys.OPENAI_API_KEY)
            .timeout(ofSeconds(60))
            .build();

    static class Simple_Structured_Prompt_Example {

        @StructuredPrompt("Create a recipe of a {{dish}} that can be prepared using only {{ingredients}}")
        static class CreateRecipePrompt {

            private String dish;
            private List<String> ingredients;
        }

        public static void main(String[] args) {

            CreateRecipePrompt prompt = new CreateRecipePrompt();
            prompt.dish = "salad";
            prompt.ingredients = asList("cucumber", "tomato", "feta", "onion", "olives");

            AiMessage aiMessage = model.sendUserMessage(prompt);
            System.out.println(aiMessage.text());
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
            prompt.ingredients = asList("cucumber", "tomato", "feta", "onion", "olives");

            AiMessage aiMessage = model.sendUserMessage(prompt);
            System.out.println(aiMessage.text());
        }
    }
}
