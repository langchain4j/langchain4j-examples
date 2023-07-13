import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;

import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_4;

public class HelloWorldExample {

    public static void main(String[] args) {

        // Import your OpenAI/HuggingFace API key
        String apiKey = System.getenv("OPENAI_API_KEY");

        // Create an instance of a model
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(GPT_4) // TODO 0613
                .build();

        long max = 0;
        long sum = 0;
        int cycles = 10;
        for (int i = 0; i < cycles; i++) {
            long start = System.currentTimeMillis();
            AiMessage answer = model.sendUserMessage("Write a sentence in 100 words").get();
            System.out.println(answer.text());
            long stop = System.currentTimeMillis();
            long l = stop - start;
            max = Math.max(l, max);
            sum += l;
            System.out.println(l);
        }

        System.out.println("Max: " + max);
        System.out.println("Mean: " + sum / cycles);
    }
}
