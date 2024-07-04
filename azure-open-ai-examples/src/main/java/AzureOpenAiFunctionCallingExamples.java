import dev.langchain4j.agent.tool.DefaultToolExecutor;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolExecutor;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class AzureOpenAiFunctionCallingExamples {

    static class Weather_From_Manual_Configuration {

        static ChatLanguageModel azureOpenAiModel = AzureOpenAiChatModel.builder()
                .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                .deploymentName(System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME"))
                .temperature(0.7)
                .logRequestsAndResponses(true)
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
            AiMessage aiMessage = azureOpenAiModel.generate(chatMessages, toolSpecifications).content();
            List<ToolExecutionRequest> toolExecutionRequests = aiMessage.toolExecutionRequests();
            System.out.println("Out of the " + toolSpecifications.size() + " functions declared in WeatherTools, " + toolExecutionRequests.size() + " will be invoked:");
            toolExecutionRequests.forEach(toolExecutionRequest -> {
                System.out.println("Function name: " + toolExecutionRequest.name());
                System.out.println("Function args:" + toolExecutionRequest.arguments());
            });
            chatMessages.add(aiMessage);

            // STEP 3: User execute function to obtain tool results
            toolExecutionRequests.forEach(toolExecutionRequest -> { // return all tools to call to answer the user query
                ToolExecutor toolExecutor = null;
                try {
                    toolExecutor = new DefaultToolExecutor(weatherTools, weatherTools.getClass().getDeclaredMethod(toolExecutionRequest.name(), String.class));
                    System.out.println("Now let's execute the function " + toolExecutionRequest.name());
                    String result = toolExecutor.execute(toolExecutionRequest, UUID.randomUUID().toString());
                    ToolExecutionResultMessage toolExecutionResultMessages = ToolExecutionResultMessage.from(toolExecutionRequest, result);
                    chatMessages.add(toolExecutionResultMessages);
                } catch (NoSuchMethodException e) {
                    System.out.println(toolExecutionRequest.name() + " method with String as a parameter does not exist");
                    ;
                }
            });

            // STEP 4: Model generate final response
            AiMessage finalResponse = azureOpenAiModel.generate(chatMessages).content();
            System.out.println(finalResponse.text()); //According to the payment data, the payment status of transaction T1005 is Pending.
        }
    }

    static class WeatherTools {

        @Tool("Returns the weather forecast for tomorrow for a given city")
        String getWeather(@P("The city for which the weather forecast should be returned") String city) {
            return "The weather in " + city + " is 25°C";
        }

        @Tool("Returns the weather forecast for tomorrow for a given city index")
        String getWeather(@P("The city index for which the weather forecast should be returned") int cityIndex) {
            String[] city = {"New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose"};
            return "The weather in " + city[cityIndex] + " is 25°C";
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
