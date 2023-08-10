package embedding.model;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;

public class HuggingFaceEmbeddingModelExample {

    public static void main(String[] args) {

        EmbeddingModel embeddingModel = HuggingFaceEmbeddingModel.builder()
                .accessToken(System.getenv("HF_API_KEY"))
                .modelId("sentence-transformers/all-MiniLM-L6-v2")
                .waitForModel(true)
                .build();

        Embedding embedding = embeddingModel.embed("Hello, how are you?");
        System.out.println(embedding);
    }
}
