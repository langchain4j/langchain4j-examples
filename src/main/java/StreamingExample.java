import dev.langchain4j.model.StreamingResultHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingLanguageModel;

import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static dev.langchain4j.model.openai.OpenAiModelName.TEXT_DAVINCI_003;

public class StreamingExample {

    static class StreamableChatLanguageModelExample {

        public static void main(String[] args) {

            StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
                    .modelName(GPT_3_5_TURBO)
                    .build();

            model.sendUserMessage("Tell me a joke", new StreamingResultHandler() {

                @Override
                public void onPartialResult(String partialResult) {
                    System.out.println("Partial result: '" + partialResult + "'");
                }

                @Override
                public void onComplete() {
                    System.out.println("Streaming completed");
                }

                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                }
            });
        }
    }

    static class StreamableLanguageModelExample {

        public static void main(String[] args) {

            StreamingLanguageModel model = OpenAiStreamingLanguageModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
                    .modelName(TEXT_DAVINCI_003)
                    .build();

            model.process("Tell me a joke", new StreamingResultHandler() {

                @Override
                public void onPartialResult(String partialResult) {
                    System.out.println("Partial result: '" + partialResult + "'");
                }

                @Override
                public void onComplete() {
                    System.out.println("Streaming completed");
                }

                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                }
            });
        }
    }
}
