import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.HashMap;
import java.util.Map;

public class PromptTemplateExamples {

    static class PromptTemplate_with_One_Variable_Example {

        public static void main(String[] args) {

            PromptTemplate promptTemplate = PromptTemplate.from("Hi, my name is {{it}}.");

            Prompt prompt = promptTemplate.apply("John");

            System.out.println(prompt.text()); // Hi, my name is John.
        }
    }

    static class PromptTemplate_With_Multiple_Variables_Example {

        public static void main(String[] args) {

            PromptTemplate promptTemplate = PromptTemplate.from("Hi, my name is {{name}}. I am {{age}} years old.");

            Map<String, Object> variables = new HashMap<>();
            variables.put("name", "John");
            variables.put("age", 35);

            Prompt prompt = promptTemplate.apply(variables);

            System.out.println(prompt.text()); // Hi, my name is John. I am 35 years old.
        }
    }
}
