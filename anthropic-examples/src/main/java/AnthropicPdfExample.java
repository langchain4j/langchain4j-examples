import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Base64;

import static dev.langchain4j.internal.Utils.readBytes;
import static org.assertj.core.api.Assertions.assertThat;

class AnthropicPdfExample {

    ChatModel model = AnthropicChatModel.builder()
            // API key can be created here: https://console.anthropic.com/settings/keys
            .apiKey(System.getenv("ANTHROPIC_API_KEY"))
            .modelName("claude-sonnet-4-5-20250514")
            .logRequests(true)
            .logResponses(true)
            .build();

    @Test
    void AnthropicChatModel_with_PDF_via_URL_Example() {

        UserMessage userMessage = UserMessage.from(
                PdfFileContent.from(URI.create("https://assets.anthropic.com/m/1cd9d098ac3e6467/original/Claude-3-Model-Card-October-Addendum.pdf")),
                TextContent.from("What are the key findings in this document?")
        );

        ChatResponse chatResponse = model.chat(userMessage);

        assertThat(chatResponse.aiMessage().text()).isNotBlank();
    }

    @Test
    void AnthropicChatModel_with_PDF_via_Base64_Example() {

        byte[] pdfBytes = readBytes("https://assets.anthropic.com/m/1cd9d098ac3e6467/original/Claude-3-Model-Card-October-Addendum.pdf");
        String base64EncodedPdf = Base64.getEncoder().encodeToString(pdfBytes);

        UserMessage userMessage = UserMessage.from(
                PdfFileContent.from(base64EncodedPdf, "application/pdf"),
                TextContent.from("Summarize this document.")
        );

        ChatResponse chatResponse = model.chat(userMessage);

        assertThat(chatResponse.aiMessage().text()).isNotBlank();
    }
}
