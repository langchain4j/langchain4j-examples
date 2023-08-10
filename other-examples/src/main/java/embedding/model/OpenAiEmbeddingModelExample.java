package embedding.model;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

import static dev.langchain4j.model.openai.OpenAiModelName.TEXT_EMBEDDING_ADA_002;

public class OpenAiEmbeddingModelExample {

    public static void main(String[] args) {

        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey("demo")
                .modelName(TEXT_EMBEDDING_ADA_002)
                .build();

        Embedding embedding = embeddingModel.embed("Hello, how are you?");
        System.out.println(embedding);
    }
}
