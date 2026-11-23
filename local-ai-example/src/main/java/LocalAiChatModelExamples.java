import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.localai.LocalAiChatModel;
import dev.langchain4j.model.output.Response;

import java.util.Collections;
import java.util.List;

public class LocalAiChatModelExamples extends AbstractLocalAiInfrastructure {
    static ChatLanguageModel model = LocalAiChatModel.builder()
            .baseUrl(localAi.getBaseUrl())
            .modelName("ggml-gpt4all-j")
            .maxTokens(3)
            .logRequests(true)
            .logResponses(true)
            .build();

    static class Simple_Prompt {
        public static void main(String[] args) {
            String answer = model.generate("better go home and weave a net than to stand by the pond longing for fish.");

            System.out.println(answer);
        }
    }

    static class Simple_Message_Prompt {
        public static void main(String[] args) {
            UserMessage userMessage = UserMessage.from("better go home and weave a net than to stand by the pond longing for fish.");
            List<ChatMessage> messages = Collections.singletonList(userMessage);
            Response<AiMessage> response = model.generate(messages);

            System.out.println(response);
        }
    }
}
