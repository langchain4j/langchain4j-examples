import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.Map;

public class PromptTemplateExamples {

    static class PromptTemplateWithOneVariable {

        public static void main(String[] args) {

            PromptTemplate promptTemplate = PromptTemplate.from("Hi, my name is {{name}}.");

            Prompt prompt = promptTemplate.apply("name", "John");

            System.out.println(prompt.text());
        }
    }

    static class PromptTemplateWithMultipleVariables {

        public static void main(String[] args) {

            PromptTemplate promptTemplate = PromptTemplate.from("Hi, my name is {{name}}. I am {{age}} years old.");

            Prompt prompt = promptTemplate.apply(Map.of(
                    "name", "John",
                    "age", 35
            ));

            System.out.println(prompt.text());
        }
    }
}
