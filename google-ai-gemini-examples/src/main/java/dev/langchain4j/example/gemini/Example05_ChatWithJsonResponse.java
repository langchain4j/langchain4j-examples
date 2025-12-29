package dev.langchain4j.example.gemini;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.data.message.UserMessage;

import java.util.List;

/**
 * Demonstrates structured JSON responses with Google AI Gemini.
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Define a JSON schema for the expected response structure</li>
 *   <li>Configure the model to return responses matching the schema</li>
 *   <li>Parse structured data from model responses</li>
 * </ul>
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 *
 * <p>Learn <a href="https://docs.langchain4j.dev/tutorials/structured-outputs">more</a></p>
 */
public class Example05_ChatWithJsonResponse {

    public static void main(String[] args) {
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
                .modelName("gemini-2.5-flash-lite")
                .build();

        JsonObjectSchema personSchema = JsonObjectSchema.builder()
                .addStringProperty("name")
                .addIntegerProperty("age")
                .addStringProperty("occupation")
                .addProperty("hobbies", JsonArraySchema.builder()
                        .items(new JsonStringSchema())
                        .build())
                .required("name", "age", "occupation", "hobbies")
                .build();

        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormat.JSON.type())
                .jsonSchema(JsonSchema.builder().name("person").rootElement(personSchema).build())
                .build();

        ChatRequest request = ChatRequest.builder()
                .messages(List.of(UserMessage.from(
                        "Generate a fictional person with a name, age, occupation, and 3 hobbies.")))
                .responseFormat(responseFormat)
                .build();

        ChatResponse response = model.chat(request);

        System.out.println("Structured JSON response:");
        System.out.println(response.aiMessage().text());
    }
}