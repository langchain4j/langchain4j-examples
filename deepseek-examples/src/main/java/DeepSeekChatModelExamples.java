import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Examples of using DeepSeek chat models with LangChain4j.
 *
 * DeepSeek exposes an OpenAI-compatible API, so it can be used through the
 * {@code langchain4j-open-ai} module by pointing {@code baseUrl} to
 * {@code https://api.deepseek.com}.
 *
 * Supported model names (see https://api-docs.deepseek.com/quick_start/pricing):
 * <ul>
 *     <li>{@code deepseek-chat} - the general-purpose chat model</li>
 *     <li>{@code deepseek-reasoner} - the reasoning model</li>
 * </ul>
 *
 * Get your API key at https://platform.deepseek.com/api_keys
 */
public class DeepSeekChatModelExamples {

    static class Simple_Prompt {

        public static void main(String[] args) {

            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(System.getenv("DEEPSEEK_API_KEY")) // Please use your own DeepSeek API key
                    .baseUrl("https://api.deepseek.com")
                    .modelName("deepseek-chat")
                    .logRequests(true)
                    .logResponses(true)
                    .build();

            String answer = model.chat("Explain in one sentence why Java is platform independent");

            System.out.println(answer);
        }
    }

    static class Reasoning_Model {

        public static void main(String[] args) {

            ChatModel model = OpenAiChatModel.builder()
                    .apiKey(System.getenv("DEEPSEEK_API_KEY")) // Please use your own DeepSeek API key
                    .baseUrl("https://api.deepseek.com")
                    .modelName("deepseek-reasoner")
                    .logRequests(true)
                    .logResponses(true)
                    .build();

            String answer = model.chat("A farmer has 17 sheep, all but 9 die. How many are left?");

            System.out.println(answer);
        }
    }
}
