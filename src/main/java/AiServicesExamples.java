import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AiServicesExamples {

    private static final ChatLanguageModel MODEL = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
            .build();


    static class Simple_AI_Service_Example {

        interface Comedian {

            String tellMeJoke();

            String tellMeJokeAbout(String topic);

            boolean isItFunny(String joke);

            Funniness evaluate(String joke);
        }

        enum Funniness {
            VERY_FUNNY, FUNNY, MEH, NOT_FUNNY
        }

        public static void main(String[] args) {

            Comedian comedian = AiServices.create(Comedian.class, MODEL);

            String joke = comedian.tellMeJoke();
            System.out.println(joke); // Why couldn't the bicycle stand up by itself? Because it was two-tired!

            String anotherJoke = comedian.tellMeJokeAbout("tomato");
            System.out.println(anotherJoke); // Why did the tomato turn red? Because it saw the salad dressing!

            boolean funny = comedian.isItFunny(joke);
            System.out.println(funny); // true

            Funniness funniness = comedian.evaluate(joke);
            System.out.println(funniness); // MEH
        }
    }


    static class Date_and_Time_Extracting_AI_Service_Example {

        interface DateTimeExtractor {

            LocalDate extractDateFrom(String text);

            LocalTime extractTimeFrom(String text);

            LocalDateTime extractDateTimeFrom(String text);
        }

        public static void main(String[] args) {

            DateTimeExtractor extractor = AiServices.create(DateTimeExtractor.class, MODEL);

            String text = "The tranquility pervaded the evening of 1968, just fifteen minutes shy of midnight," +
                    " following the celebrations of Independence Day.";

            LocalDate date = extractor.extractDateFrom(text);
            System.out.println(date); // 1968-07-04

            LocalTime time = extractor.extractTimeFrom(text);
            System.out.println(time); // 23:45

            LocalDateTime dateTime = extractor.extractDateTimeFrom(text);
            System.out.println(dateTime); // 1968-07-04T23:45
        }
    }


    static class POJO_Extracting_AI_Service_Example {

        static class Person {

            private String firstName;
            private String lastName;
            private LocalDate birthDate;

            @Override
            public String toString() {
                return "Person {" +
                        " firstName = \"" + firstName + "\"" +
                        ", lastName = \"" + lastName + "\"" +
                        ", birthDate = " + birthDate +
                        " }";
            }
        }

        interface PersonExtractor {

            Person extractPersonFrom(String text);
        }

        public static void main(String[] args) {

            PersonExtractor extractor = AiServices.create(PersonExtractor.class, MODEL);

            String text = "In 1968, amidst the fading echoes of Independence Day, "
                    + "a child named John arrived under the calm evening sky. "
                    + "This newborn, bearing the surname Doe, marked the start of a new journey.";


            Person person = extractor.extractPersonFrom(text);

            System.out.println(person); // Person { firstName = "John", lastName = "Doe", birthDate = 1968-07-04 }
        }
    }


    static class POJO_With_Descriptions_Extracting_AI_Service_Example {

        static class Recipe {

            @Description("short title, 3 words maximum")
            private String title;

            @Description("short description, 2 sentences maximum")
            private String description;

            @Description("each step should be described in 4 words, steps should rhyme")
            private List<String> steps;

            private Integer preparationTimeMinutes;

            @Override
            public String toString() {
                return "Recipe {" +
                        " title = \"" + title + "\"" +
                        ", description = \"" + description + "\"" +
                        ", steps = " + steps +
                        ", preparationTimeMinutes = " + preparationTimeMinutes +
                        " }";
            }
        }

        @StructuredPrompt("Create a recipe of a {{dish}} that can be prepared using only {{ingredients}}")
        static class CreateRecipePrompt {

            private String dish;
            private List<String> ingredients;
        }

        interface Chef {

            Recipe createRecipeFrom(String... ingredients);

            Recipe createRecipe(CreateRecipePrompt prompt);
        }

        public static void main(String[] args) {

            Chef chef = AiServices.create(Chef.class, MODEL);

            Recipe recipe = chef.createRecipeFrom("cucumber", "tomato", "feta", "onion", "olives");

            System.out.println(recipe);
            // Recipe {
            //     title = "Greek Salad",
            //     description = "A refreshing mix of veggies and feta cheese in a zesty dressing.",
            //     steps = [
            //         "Chop cucumber and tomato",
            //         "Add onion and olives",
            //         "Crumble feta on top",
            //         "Drizzle with dressing and enjoy!"
            //     ],
            //     preparationTimeMinutes = 10
            // }


            CreateRecipePrompt prompt = new CreateRecipePrompt();
            prompt.dish = "salad";
            prompt.ingredients = List.of("cucumber", "tomato", "feta", "onion", "olives");

            Recipe anotherRecipe = chef.createRecipe(prompt);
            System.out.println(anotherRecipe);
        }
    }


    static class AI_Service_with_System_Message_Example {

        interface Chef {

            @SystemMessage("You are a professional chef. You are friendly, polite and concise.")
            String answer(String question);
        }

        public static void main(String[] args) {

            Chef chef = AiServices.create(Chef.class, MODEL);

            String answer = chef.answer("How long should I grill chicken?");
            System.out.println(answer); // It depends on the thickness of the chicken. As a general rule, ...
        }
    }

    static class AI_Service_with_System_and_User_Messages_Example {

        interface TextUtils {

            @SystemMessage("You are a professional translator into {{language}}")
            @UserMessage("Translate the following text: {{text}}")
            String translate(@V("text") String text, @V("language") String language);

            @SystemMessage("Summarize every message from user in {{n}} bullet points. Provide only bullet points.")
            List<String> summarize(@UserMessage String text, @V("n") int n);
        }

        public static void main(String[] args) {

            TextUtils utils = AiServices.create(TextUtils.class, MODEL);

            String translation = utils.translate("Hello, how are you?", "italian");
            System.out.println(translation); // Ciao, come stai?


            String text = "AI, or artificial intelligence, is a branch of computer science that aims to create " +
                    "machines that mimic human intelligence. This can range from simple tasks such as recognizing " +
                    "patterns or speech to more complex tasks like making decisions or predictions.";

            List<String> bulletPoints = utils.summarize(text, 3);
            System.out.println(bulletPoints);
            // [
            //     "- AI is a branch of computer science",
            //     "- It aims to create machines that mimic human intelligence",
            //     "- It can perform simple or complex tasks"
            // ]
        }
    }
}
