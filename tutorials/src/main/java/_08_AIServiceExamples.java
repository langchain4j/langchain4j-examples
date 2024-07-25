import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;

public class _08_AIServiceExamples {

    static ChatLanguageModel model = OpenAiChatModel.builder()
            .apiKey(ApiKeys.OPENAI_API_KEY)
            .timeout(ofSeconds(60))
            .build();

    ////////////////// SIMPLE EXAMPLE //////////////////////

    static class Simple_AI_Service_Example {

        interface Assistant {

            String chat(String message);
        }

        public static void main(String[] args) {

            Assistant assistant = AiServices.create(Assistant.class, model);

            String userMessage = "Translate 'Plus-Values des cessions de valeurs mobilières, de droits sociaux et gains assimilés'";

            String answer = assistant.chat(userMessage);

            System.out.println(answer);
        }
    }

    ////////////////// WITH MESSAGE AND VARIABLES //////////////////////

    static class AI_Service_with_System_Message_Example {

        interface Chef {

            @SystemMessage("You are a professional chef. You are friendly, polite and concise.")
            String answer(String question);
        }

        public static void main(String[] args) {

            Chef chef = AiServices.create(Chef.class, model);

            String answer = chef.answer("How long should I grill chicken?");

            System.out.println(answer); // Grilling chicken usually takes around 10-15 minutes per side ...
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

            TextUtils utils = AiServices.create(TextUtils.class, model);

            String translation = utils.translate("Hello, how are you?", "italian");
            System.out.println(translation); // Ciao, come stai?

            String text = "AI, or artificial intelligence, is a branch of computer science that aims to create "
                    + "machines that mimic human intelligence. This can range from simple tasks such as recognizing "
                    + "patterns or speech to more complex tasks like making decisions or predictions.";

            List<String> bulletPoints = utils.summarize(text, 3);
            bulletPoints.forEach(System.out::println);
            // [
            // "- AI is a branch of computer science",
            // "- It aims to create machines that mimic human intelligence",
            // "- It can perform simple or complex tasks"
            // ]
        }
    }

    //////////////////// EXTRACTING DIFFERENT DATA TYPES ////////////////////

    static class Sentiment_Extracting_AI_Service_Example {

        enum Sentiment {
            POSITIVE, NEUTRAL, NEGATIVE
        }

        interface SentimentAnalyzer {

            @UserMessage("Analyze sentiment of {{it}}")
            Sentiment analyzeSentimentOf(String text);

            @UserMessage("Does {{it}} have a positive sentiment?")
            boolean isPositive(String text);
        }

        public static void main(String[] args) {

            SentimentAnalyzer sentimentAnalyzer = AiServices.create(SentimentAnalyzer.class, model);

            Sentiment sentiment = sentimentAnalyzer.analyzeSentimentOf("It is good!");
            System.out.println(sentiment); // POSITIVE

            boolean positive = sentimentAnalyzer.isPositive("It is bad!");
            System.out.println(positive); // false
        }
    }

    static class Hotel_Review_AI_Service_Example {

        public enum IssueCategory {

            @Description("The feedback mentions issues with the hotel's maintenance, such as air conditioning and plumbing problems")
            MAINTENANCE_ISSUE,

            @Description("The feedback mentions issues with the service provided, such as slow room service")
            SERVICE_ISSUE,

            @Description("The feedback mentions issues affecting the comfort of the stay, such as uncomfortable room conditions")
            COMFORT_ISSUE,

            @Description("The feedback mentions issues with hotel facilities, such as problems with the bathroom plumbing")
            FACILITY_ISSUE,

            @Description("The feedback mentions issues with the cleanliness of the hotel, such as dust and stains")
            CLEANLINESS_ISSUE,

            @Description("The feedback mentions issues with internet connectivity, such as unreliable Wi-Fi")
            CONNECTIVITY_ISSUE,

            @Description("The feedback mentions issues with the check-in process, such as it being tedious and time-consuming")
            CHECK_IN_ISSUE,

            @Description("The feedback mentions a general dissatisfaction with the overall hotel experience due to multiple issues")
            OVERALL_EXPERIENCE_ISSUE
        }

        interface HotelReviewIssueAnalyzer {

            @UserMessage("Please analyse the following review: |||{{it}}|||")
            List<IssueCategory> analyzeReview(String review);
        }

        public static void main(String[] args) {

            HotelReviewIssueAnalyzer hotelReviewIssueAnalyzer = AiServices.create(HotelReviewIssueAnalyzer.class, model);

            String review = "Our stay at hotel was a mixed experience. The location was perfect, just a stone's throw away " +
                    "from the beach, which made our daily outings very convenient. The rooms were spacious and well-decorated, " +
                    "providing a comfortable and pleasant environment. However, we encountered several issues during our " +
                    "stay. The air conditioning in our room was not functioning properly, making the nights quite uncomfortable. " +
                    "Additionally, the room service was slow, and we had to call multiple times to get extra towels. Despite the " +
                    "friendly staff and enjoyable breakfast buffet, these issues significantly impacted our stay.";

            List<IssueCategory> issueCategories = hotelReviewIssueAnalyzer.analyzeReview(review);

            // Should output [MAINTENANCE_ISSUE, SERVICE_ISSUE, COMFORT_ISSUE, OVERALL_EXPERIENCE_ISSUE]
            System.out.println(issueCategories);
        }
    }

    static class Number_Extracting_AI_Service_Example {

        interface NumberExtractor {

            @UserMessage("Extract number from {{it}}")
            int extractInt(String text);

            @UserMessage("Extract number from {{it}}")
            long extractLong(String text);

            @UserMessage("Extract number from {{it}}")
            BigInteger extractBigInteger(String text);

            @UserMessage("Extract number from {{it}}")
            float extractFloat(String text);

            @UserMessage("Extract number from {{it}}")
            double extractDouble(String text);

            @UserMessage("Extract number from {{it}}")
            BigDecimal extractBigDecimal(String text);
        }

        public static void main(String[] args) {

            NumberExtractor extractor = AiServices.create(NumberExtractor.class, model);

            String text = "After countless millennia of computation, the supercomputer Deep Thought finally announced "
                    + "that the answer to the ultimate question of life, the universe, and everything was forty two.";

            int intNumber = extractor.extractInt(text);
            System.out.println(intNumber); // 42

            long longNumber = extractor.extractLong(text);
            System.out.println(longNumber); // 42

            BigInteger bigIntegerNumber = extractor.extractBigInteger(text);
            System.out.println(bigIntegerNumber); // 42

            float floatNumber = extractor.extractFloat(text);
            System.out.println(floatNumber); // 42.0

            double doubleNumber = extractor.extractDouble(text);
            System.out.println(doubleNumber); // 42.0

            BigDecimal bigDecimalNumber = extractor.extractBigDecimal(text);
            System.out.println(bigDecimalNumber); // 42.0
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

            DateTimeExtractor extractor = AiServices.create(DateTimeExtractor.class, model);

            String text = "The tranquility pervaded the evening of 1968, just fifteen minutes shy of midnight,"
                    + " following the celebrations of Independence Day.";

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

            PersonExtractor extractor = AiServices.create(PersonExtractor.class, model);

            String text = "In 1968, amidst the fading echoes of Independence Day, "
                    + "a child named John arrived under the calm evening sky. "
                    + "This newborn, bearing the surname Doe, marked the start of a new journey.";

            Person person = extractor.extractPersonFrom(text);

            System.out.println(person); // Person { firstName = "John", lastName = "Doe", birthDate = 1968-07-04 }
        }
    }

    ////////////////////// DESCRIPTIONS ////////////////////////

    static class POJO_With_Descriptions_Extracting_AI_Service_Example {

        static class Recipe {

            @Description("short title, 3 words maximum")
            private String title;

            @Description("short description, 2 sentences maximum")
            private String description;

            @Description("each step should be described in 6 to 8 words, steps should rhyme with each other")
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

            Chef chef = AiServices.create(Chef.class, model);

            Recipe recipe = chef.createRecipeFrom("cucumber", "tomato", "feta", "onion", "olives", "lemon");

            System.out.println(recipe);
            // Recipe {
            // title = "Greek Salad",
            // description = "A refreshing mix of veggies and feta cheese in a zesty
            // dressing.",
            // steps = [
            // "Chop cucumber and tomato",
            // "Add onion and olives",
            // "Crumble feta on top",
            // "Drizzle with dressing and enjoy!"
            // ],
            // preparationTimeMinutes = 10
            // }

            CreateRecipePrompt prompt = new CreateRecipePrompt();
            prompt.dish = "oven dish";
            prompt.ingredients = asList("cucumber", "tomato", "feta", "onion", "olives", "potatoes");

            Recipe anotherRecipe = chef.createRecipe(prompt);
            System.out.println(anotherRecipe);
            // Recipe ...
        }
    }


    ////////////////////////// WITH MEMORY /////////////////////////

    static class ServiceWithMemoryExample {

        interface Assistant {

            String chat(String message);
        }

        public static void main(String[] args) {

            ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatLanguageModel(model)
                    .chatMemory(chatMemory)
                    .build();

            String answer = assistant.chat("Hello! My name is Klaus.");
            System.out.println(answer); // Hello Klaus! How can I assist you today?

            String answerWithName = assistant.chat("What is my name?");
            System.out.println(answerWithName); // Your name is Klaus.
        }
    }

    static class ServiceWithMemoryForEachUserExample {

        interface Assistant {

            String chat(@MemoryId int memoryId, @UserMessage String userMessage);
        }

        public static void main(String[] args) {

            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatLanguageModel(model)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                    .build();

            System.out.println(assistant.chat(1, "Hello, my name is Klaus"));
            // Hi Klaus! How can I assist you today?

            System.out.println(assistant.chat(2, "Hello, my name is Francine"));
            // Hello Francine! How can I assist you today?

            System.out.println(assistant.chat(1, "What is my name?"));
            // Your name is Klaus.

            System.out.println(assistant.chat(2, "What is my name?"));
            // Your name is Francine.
        }
    }
}
