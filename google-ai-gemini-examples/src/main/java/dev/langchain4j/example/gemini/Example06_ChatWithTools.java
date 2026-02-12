package dev.langchain4j.example.gemini;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates tool/function calling with Google AI Gemini.
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Define tools using the {@link Tool} annotation</li>
 *   <li>Provide tool specifications to the model</li>
 *   <li>Handle tool execution requests and return results</li>
 *   <li>Continue the conversation with tool results</li>
 * </ul>
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 *
 * <p>Learn <a href="https://docs.langchain4j.dev/tutorials/tools">more</a></p>
 */
public class Example06_ChatWithTools {

    public static void main(String[] args) {
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
                .modelName("gemini-2.5-flash-lite")
                .build();

        Calculator calculator = new Calculator();

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(UserMessage.from("What is 25 multiplied by 17, and then add 123 to the result?"));

        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .toolSpecifications(ToolSpecifications.toolSpecificationsFrom(calculator))
                .build();

        ChatResponse response = model.chat(request);
        AiMessage aiMessage = response.aiMessage();

        // Process tool calls if any
        while (aiMessage.hasToolExecutionRequests()) {
            System.out.println("Model requested tool execution:");

            messages.add(aiMessage);

            for (ToolExecutionRequest toolRequest : aiMessage.toolExecutionRequests()) {
                System.out.println("  Tool: " + toolRequest.name());
                System.out.println("  Arguments: " + toolRequest.arguments());

                String result = executeCalculatorTool(calculator, toolRequest);
                System.out.println("  Result: " + result);

                messages.add(ToolExecutionResultMessage.from(toolRequest, result));
            }

            // Continue the conversation with tool results
            request = ChatRequest.builder()
                    .messages(messages)
                    .toolSpecifications(ToolSpecifications.toolSpecificationsFrom(calculator))
                    .build();

            response = model.chat(request);
            aiMessage = response.aiMessage();
        }

        System.out.println("\nFinal response: " + aiMessage.text());
    }

    private static String executeCalculatorTool(Calculator calculator, ToolExecutionRequest request) {
        String args = request.arguments();

        // Simple JSON parsing for demo purposes
        double a = extractNumber(args, "a");
        double b = extractNumber(args, "b");

        return switch (request.name()) {
            case "add" -> String.valueOf(calculator.add(a, b));
            case "multiply" -> String.valueOf(calculator.multiply(a, b));
            default -> "Unknown tool";
        };
    }

    private static double extractNumber(String json, String field) {
        String pattern = "\"" + field + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return 0;

        int colonIdx = json.indexOf(":", idx);
        int endIdx = json.indexOf(",", colonIdx);
        if (endIdx == -1) endIdx = json.indexOf("}", colonIdx);

        return Double.parseDouble(json.substring(colonIdx + 1, endIdx).trim());
    }

    static class Calculator {

        @Tool("Adds two numbers")
        public double add(double a, double b) {
            return a + b;
        }

        @Tool("Multiplies two numbers")
        public double multiply(double a, double b) {
            return a * b;
        }
    }
}