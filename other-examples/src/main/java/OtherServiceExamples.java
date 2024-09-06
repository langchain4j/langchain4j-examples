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
import java.util.List;
import java.util.function.Function;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static java.util.Arrays.asList;

public class OtherServiceExamples {

    static ChatLanguageModel chatLanguageModel = OpenAiChatModel.builder()
            .apiKey(ApiKeys.OPENAI_API_KEY)
            .modelName(GPT_4_O_MINI)
            .logRequests(true)
            .logResponses(true)
            .build();

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

            NumberExtractor extractor = AiServices.create(NumberExtractor.class, chatLanguageModel);

            String text = "After countless millennia of computation, the supercomputer Deep Thought finally announced " +
                    "that the answer to the ultimate question of life, the universe, and everything was forty two.";

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

            @Description("first name of a person")
            // you can add an optional description to help an LLM have a better understanding
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

            @UserMessage("Extract a person from the following text: {{it}}")
            Person extractPersonFrom(String text);
        }

        public static void main(String[] args) {

            ChatLanguageModel chatLanguageModel = OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName(GPT_4_O_MINI)
                    // When extracting POJOs with the LLM that supports the "json mode" feature
                    // (e.g., OpenAI, Azure OpenAI, Vertex AI Gemini, Ollama, etc.),
                    // it is advisable to enable it (json mode) to get more reliable results.
                    // When using this feature, LLM will be forced to output a valid JSON.
                    // Please note that this feature is not (yet) supported when using "demo" key.
//                    .responseFormat("json_schema")
//                    .strictJsonSchema(true) // https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-json-mode
                    .logRequests(true)
                    .logResponses(true)
                    .build();

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

            ChatLanguageModel chatLanguageModel = OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName(GPT_4_O_MINI)
                    // When extracting POJOs with the LLM that supports the "json mode" feature
                    // (e.g., OpenAI, Azure OpenAI, Vertex AI Gemini, Ollama, etc.),
                    // it is advisable to enable it (json mode) to get more reliable results.
                    // When using this feature, LLM will be forced to output a valid JSON.
                    // Please note that this feature is not (yet) supported when using "demo" key.
                    .responseFormat("json_schema")
                    .strictJsonSchema(true) // https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-json-mode
                    .logRequests(true)
                    .logResponses(true)
                    .build();

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
            prompt.ingredients = asList("cucumber", "tomato", "feta", "onion", "olives");

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


    static class AI_Service_with_System_and_User_Messages_loaded_from_resources_Example {

        interface TextUtils {

            @SystemMessage(fromResource = "/translator-system-prompt-template.txt")
            @UserMessage(fromResource = "/translator-user-prompt-template.txt")
            String translate(@V("text") String text, @V("language") String language);
        }

        public static void main(String[] args) {

            TextUtils utils = AiServices.create(TextUtils.class, chatLanguageModel);

            String translation = utils.translate("Hello, how are you?", "italian");
            System.out.println(translation); // Ciao, come stai?
        }
    }


    static class AI_Service_with_UserName_Example {

        interface Assistant {

            String chat(@UserName String name, @UserMessage String message);
        }

        public static void main(String[] args) {

            Assistant assistant = AiServices.create(Assistant.class, chatLanguageModel);

            String answer = assistant.chat("Klaus", "Hi, tell me my name if you see it.");
            System.out.println(answer); // Hello! Your name is Klaus. How can I assist you today?
        }
    }

    static class AI_Service_with_Dynamic_System_Message_Example {

        interface Assistant {

            String chat(@MemoryId String memoryId, @UserMessage String userMessage);
        }

        public static void main(String[] args) {

            Function<Object, String> systemMessageProvider = (memoryId) -> {
                if (memoryId.equals("1")) {
                    return "You are a helpful assistant. The user prefers to be called 'Your Majesty'.";
                } else {
                    return "You are a helpful assistant.";
                }
            };

            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatLanguageModel(chatLanguageModel)
                    .systemMessageProvider(systemMessageProvider)
                    .build();

            System.out.println(assistant.chat("1", "Hi")); // Hello, Your Majesty! How may I assist you today?
            System.out.println(assistant.chat("2", "Hi")); // Hello! How can I assist you today?
        }
    }
}
