import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.input.structured.StructuredPromptProcessor;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;

public class _03_PromptTemplate {

    static class Simple_Prompt_Template_Example {

        public static void main(String[] args) {

            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .modelName(GPT_4_O_MINI)
                    .timeout(ofSeconds(60))
                    .build();

            String template = "Create a recipe for a {{dishType}} with the following ingredients: {{ingredients}}";
            PromptTemplate promptTemplate = PromptTemplate.from(template);

            Map<String, Object> variables = new HashMap<>();
            variables.put("dishType", "oven dish");
            variables.put("ingredients", "potato, tomato, feta, olive oil");

            Prompt prompt = promptTemplate.apply(variables);

            String response = model.generate(prompt.text());

            System.out.println(response);
        }

    }

    static class Structured_Prompt_Template_Example {
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

            String dish;
            List<String> ingredients;

            CreateRecipePrompt(String dish, List<String> ingredients) {
                this.dish = dish;
                this.ingredients = ingredients;
            }
        }

        public static void main(String[] args) {

            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .modelName(GPT_4_O_MINI)
                    .timeout(ofSeconds(60))
                    .build();

            Structured_Prompt_Template_Example.CreateRecipePrompt createRecipePrompt = new Structured_Prompt_Template_Example.CreateRecipePrompt(
                    "salad",
                    asList("cucumber", "tomato", "feta", "onion", "olives")
            );

            Prompt prompt = StructuredPromptProcessor.toPrompt(createRecipePrompt);

            String recipe = model.generate(prompt.text());

            System.out.println(recipe);
        }
    }
}
