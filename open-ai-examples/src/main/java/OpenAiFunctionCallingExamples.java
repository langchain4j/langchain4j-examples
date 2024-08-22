import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class OpenAiFunctionCallingExamples {

    /**
     * This example demonstrates how to programmatically configure the low-level tool APIs, such as ToolSpecification,
     * ToolExecutionRequest, and ToolExecutor.
     * This sample is used in the LangChain4j tutorial: https://docs.langchain4j.dev/tutorials/tools/#low-level-tool-api.
     * But it is recommended to use higher-level APIs as demonstrated here: https://docs.langchain4j.dev/tutorials/tools/#high-level-tool-api
     * <p>
     * This sample goes through 4 different steps:
     * 1. Specify the tools (WeatherTools) and the query ("What will the weather be like in London tomorrow?")
     * 2. Model generate function arguments (model decides which tools to invoke)
     * 3. User execute function to obtain tool results (using ToolExecutor)
     * 4. Model generate final response based on the query and the tool results
     */
    static class Weather_Low_Level_Configuration {

        static ChatLanguageModel openAiModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(GPT_4_O)
                .logRequests(true)
                .logResponses(true)
                .build();

        public static void main(String[] args) throws Exception {

            // STEP 1: User specify tools and query
            // Tools
            WeatherTools weatherTools = new WeatherTools();
            List<ToolSpecification> toolSpecifications = ToolSpecifications.toolSpecificationsFrom(weatherTools);
            // User query
            List<ChatMessage> chatMessages = new ArrayList<>();
            UserMessage userMessage = userMessage("What will the weather be like in London tomorrow?");
            chatMessages.add(userMessage);


            // STEP 2: Model generate function arguments
            AiMessage aiMessage = openAiModel.generate(chatMessages, toolSpecifications).content();
            List<ToolExecutionRequest> toolExecutionRequests = aiMessage.toolExecutionRequests();
            System.out.println("Out of the " + toolSpecifications.size() + " functions declared in WeatherTools, " + toolExecutionRequests.size() + " will be invoked:");
            toolExecutionRequests.forEach(toolExecutionRequest -> {
                System.out.println("Function name: " + toolExecutionRequest.name());
                System.out.println("Function args:" + toolExecutionRequest.arguments());
            });
            chatMessages.add(aiMessage);


            // STEP 3: User execute function to obtain tool results
            toolExecutionRequests.forEach(toolExecutionRequest -> {
                ToolExecutor toolExecutor = new DefaultToolExecutor(weatherTools, toolExecutionRequest);
                System.out.println("Now let's execute the function " + toolExecutionRequest.name());
                String result = toolExecutor.execute(toolExecutionRequest, UUID.randomUUID().toString());
                ToolExecutionResultMessage toolExecutionResultMessages = ToolExecutionResultMessage.from(toolExecutionRequest, result);
                chatMessages.add(toolExecutionResultMessages);
            });


            // STEP 4: Model generate final response
            AiMessage finalResponse = openAiModel.generate(chatMessages).content();
            System.out.println(finalResponse.text()); //According to the payment data, the payment status of transaction T1005 is Pending.
        }
    }

    static class WeatherTools {

        @Tool("Returns the weather forecast for tomorrow for a given city")
        String getWeather(@P("The city for which the weather forecast should be returned") String city) {
            return "The weather tomorrow in " + city + " is 25°C";
        }

        @Tool("Returns the date for tomorrow")
        LocalDate getTomorrow() {
            return LocalDate.now().plusDays(1);
        }

        @Tool("Transforms Celsius degrees into Fahrenheit")
        double celsiusToFahrenheit(@P("The celsius degree to be transformed into fahrenheit") double celsius) {
            return (celsius * 1.8) + 32;
        }

        String iAmNotATool() {
            return "I am not a method annotated with @Tool";
        }

    }
}
