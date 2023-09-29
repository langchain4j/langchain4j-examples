import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.HashMap;
import java.util.Map;

public class PromptTemplateExamples {

    static class PromptTemplate_with_One_Variable_Example {

        public static void main(String[] args) {

            PromptTemplate promptTemplate = PromptTemplate.from("Say 'hi' in {{it}}.");

            Prompt prompt = promptTemplate.apply("German");

            System.out.println(prompt.text()); // Say 'hi' in German.
        }
    }

    static class PromptTemplate_With_Multiple_Variables_Example {

        public static void main(String[] args) {

            PromptTemplate promptTemplate = PromptTemplate.from("Say '{{text}}' in {{language}}.");

            Map<String, Object> variables = new HashMap<>();
            variables.put("text", "hi");
            variables.put("language", "German");

            Prompt prompt = promptTemplate.apply(variables);

            System.out.println(prompt.text()); // Say 'hi' in German.
        }
    }
}
