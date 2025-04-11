package converse;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.bedrock.BedrockChatModel;
import dev.langchain4j.model.bedrock.BedrockChatRequestParameters;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.nio.file.Paths;

public class BedrockChatModelExample {

    static class Simple_Prompt {

        public static void main(String[] args) {

            // For authentication, set the following environment variables:
            // AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
            // More info on creating the API keys:
            // https://docs.aws.amazon.com/bedrock/latest/userguide/api-setup.html
            ChatLanguageModel chatModel = BedrockChatModel.builder()
                    .modelId("us.amazon.nova-lite-v1:0")
                    .build();

            String joke = chatModel.chat("Tell me a joke about Java");

            System.out.println(joke);
        }
    }

    static class Image_Inputs {

        public static void main(String[] args) {

            ChatLanguageModel chatModel = BedrockChatModel.builder()
                    .modelId("us.amazon.nova-lite-v1:0")
                    .build();

            UserMessage userMessage = UserMessage.from(
                    TextContent.from("What do you see?"),
                    ImageContent.from("https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png")
            );

            ChatResponse chatResponse = chatModel.chat(userMessage);

            System.out.println(chatResponse.aiMessage().text());
        }
    }

    static class PDF_Inputs {

        public static void main(String[] args) {

            ChatLanguageModel chatModel = BedrockChatModel.builder()
                    .modelId("us.amazon.nova-lite-v1:0")
                    .build();

            UserMessage userMessage = UserMessage.from(
                    TextContent.from("Summarize attached document"),
                    PdfFileContent.from(Paths.get("bedrock-examples/src/main/resources/test-file.pdf").toUri())
            );

            ChatResponse chatResponse = chatModel.chat(userMessage);

            System.out.println(chatResponse.aiMessage().text());
        }
    }

    static class Setting_Common_ChatRequestParameters {

        public static void main(String[] args) {

            ChatRequestParameters defaultParameters = BedrockChatRequestParameters.builder()
                    .temperature(0.7)
                    .maxOutputTokens(100)
                    // there are many more common parameters, see ChatRequestParameters for more info
                    .build();

            ChatLanguageModel chatModel = BedrockChatModel.builder()
                    .modelId("us.amazon.nova-lite-v1:0")
                    .defaultRequestParameters(defaultParameters)
                    .logRequests(true)
                    .build();

            ChatRequestParameters parameters = BedrockChatRequestParameters.builder()
                    // Model choice can be overridden with request parameter
                    .modelName("anthropic.claude-3-haiku-20240307-v1:0")
                    .temperature(1.0)
                    .maxOutputTokens(50)
                    .build();

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(UserMessage.from("Tell me a funny story about Java"))
                    .parameters(parameters) // merges with and overrides default parameters
                    .build();

            ChatResponse chatResponse = chatModel.chat(chatRequest);

            System.out.println(chatResponse);
        }
    }


    static class Enable_Thinking_ChatRequestParameters {

        public static void main(String[] args) {

            ChatRequestParameters defaultParameters = BedrockChatRequestParameters.builder()
                    // enabling reasoning with a budget of 1000 tokens
                    .enableReasoning(1000L)
                    .build();

            ChatLanguageModel chatModel = BedrockChatModel.builder()
                    .modelId("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
                    .defaultRequestParameters(defaultParameters)
                    .logRequests(true)
                    .build();

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(UserMessage.from("Tell me a funny story about Java"))
                    .build();

            ChatResponse chatResponse = chatModel.chat(chatRequest);

            System.out.println(chatResponse);
        }
    }
}
