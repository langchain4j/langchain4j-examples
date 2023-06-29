import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiModerationModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AiServicesExamples {

    static String apiKey = System.getenv("OPENAI_API_KEY"); // https://platform.openai.com/account/api-keys
    static ChatLanguageModel chatLanguageModel = OpenAiChatModel.withApiKey(apiKey);

    static class Simple_AI_Service_Example {

        interface Chat {

            String chat(String userMessage);
        }

        public static void main(String[] args) {

            Chat chat = AiServices.create(Chat.class, chatLanguageModel);

            String answer = chat.chat("Hello");

            System.out.println(answer); // Hello! How can I assist you today?
        }
    }


    static class AI_Service_with_Memory_Example {

        interface Chat {

            String chat(String userMessage);
        }

        public static void main(String[] args) {

            Chat chat = AiServices.builder(Chat.class)
                    .chatLanguageModel(chatLanguageModel)
                    .chatMemory(MessageWindowChatMemory.withCapacity(10))
                    .build();

            String answer = chat.chat("Hello, my name is Klaus");
            System.out.println(answer); // Hello Klaus! How can I assist you today?

            String answerWithName = chat.chat("What is my name?");
            System.out.println(answerWithName); // Your name is Klaus.
        }
    }


    static class Sentiment_Extracting_AI_Service_Example {

        enum Sentiment {
            POSITIVE, NEUTRAL, NEGATIVE;
        }

        interface SentimentAnalyzer {

            @UserMessage("Analyze sentiment of {{it}}")
            Sentiment analyzeSentimentOf(String text);

            @UserMessage("Does {{it}} have a positive sentiment?")
            boolean isPositive(String text);
        }

        public static void main(String[] args) {

            SentimentAnalyzer sentimentAnalyzer = AiServices.create(SentimentAnalyzer.class, chatLanguageModel);

            Sentiment sentiment = sentimentAnalyzer.analyzeSentimentOf("It is good!");
            System.out.println(sentiment); // POSITIVE

            boolean positive = sentimentAnalyzer.isPositive("It is bad!");
            System.out.println(positive); // false
        }
    }


    static class Date_and_Time_Extracting_AI_Service_Example {

        interface DateTimeExtractor {

            @UserMessage("Extract date from {{it}}")
            LocalDate extractDateFrom(String text);

            @UserMessage("Extract time from {{it}}")
            LocalTime extractTimeFrom(String text);

            @UserMessage("Extract date and time from {{it}}")
            LocalDateTime extractDateTimeFrom(String text);
        }

        public static void main(String[] args) {

            DateTimeExtractor extractor = AiServices.create(DateTimeExtractor.class, chatLanguageModel);

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

            @UserMessage("Extract information about a person from {{it}}")
            Person extractPersonFrom(String text);
        }

        public static void main(String[] args) {

            PersonExtractor extractor = AiServices.create(PersonExtractor.class, chatLanguageModel);

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

            Chef chef = AiServices.create(Chef.class, chatLanguageModel);

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
            // Recipe ...
        }
    }


    static class AI_Service_with_System_Message_Example {

        interface Chef {

            @SystemMessage("You are a professional chef. You are friendly, polite and concise.")
            String answer(String question);
        }

        public static void main(String[] args) {

            Chef chef = AiServices.create(Chef.class, chatLanguageModel);

            String answer = chef.answer("How long should I grill chicken?");
            System.out.println(answer); // Grilling chicken usually takes around 10-15 minutes per side, depending on ...
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

            TextUtils utils = AiServices.create(TextUtils.class, chatLanguageModel);

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

    static class AI_Service_with_Auto_Moderation_Example {

        interface Chat {

            @Moderate
            String chat(String text);
        }

        public static void main(String[] args) {

            Chat chat = AiServices.builder(Chat.class)
                    .chatLanguageModel(chatLanguageModel)
                    .moderationModel(OpenAiModerationModel.withApiKey(System.getenv("OPENAI_API_KEY")))
                    .build();

            try {
                chat.chat("I WILL KILL YOU!!!");
            } catch (ModerationException e) {
                System.out.println(e.getMessage());
                // Text "I WILL KILL YOU!!!" violates content policy
            }
        }
    }
}
