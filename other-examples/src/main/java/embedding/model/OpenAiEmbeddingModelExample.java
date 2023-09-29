package embedding.model;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;

public class OpenAiEmbeddingModelExample {

    public static void main(String[] args) {

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.withApiKey("demo");

        Response<Embedding> response = embeddingModel.embed("Hello, how are you?");
        System.out.println(response);
    }
}
