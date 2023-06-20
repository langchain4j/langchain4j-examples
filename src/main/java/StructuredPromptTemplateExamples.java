import dev.langchain4j.model.StreamingResultHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.util.List;

public class StructuredPromptTemplateExamples {

    static class SimpleStructuredPrompt {

        @StructuredPrompt("Create a recipe of a {{dish}} that can be prepared using only {{ingredients}}")
        static class CreateRecipePrompt {

            private String dish;
            private List<String> ingredients;
        }

        public static void main(String[] args) {

            CreateRecipePrompt prompt = new CreateRecipePrompt();
            prompt.dish = "salad";
            prompt.ingredients = List.of("cucumber", "tomato", "feta", "onion", "olives");


            StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
                    .build();

            model.sendUserMessage(prompt, new StreamingResultHandler() {
                @Override
                public void onPartialResult(String partialResult) {
                    System.out.print(partialResult);
                }

                @Override
                public void onError(Throwable error) {
                }
            });
        }
    }

    static class MultiLineStructuredPrompt {

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


            StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
                    .build();

            model.sendUserMessage(prompt, new StreamingResultHandler() {
                @Override
                public void onPartialResult(String partialResult) {
                    System.out.print(partialResult);
                }

                @Override
                public void onError(Throwable error) {
                }
            });
        }
    }
}
