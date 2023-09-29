package embedding.model;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.model.output.Response;

import static java.time.Duration.ofSeconds;

public class HuggingFaceEmbeddingModelExample {

    public static void main(String[] args) {

        EmbeddingModel embeddingModel = HuggingFaceEmbeddingModel.builder()
                .accessToken(System.getenv("HF_API_KEY"))
                .modelId("sentence-transformers/all-MiniLM-L6-v2")
                .waitForModel(true)
                .timeout(ofSeconds(60))
                .build();

        Response<Embedding> response = embeddingModel.embed("Hello, how are you?");
        System.out.println(response);
    }
}
