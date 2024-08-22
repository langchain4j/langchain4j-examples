import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;

public class OpenAiEmbeddingModelExamples {

    public static void main(String[] args) {

        EmbeddingModel model = OpenAiEmbeddingModel.withApiKey("demo");

        Response<Embedding> response = model.embed("I love Java");
        Embedding embedding = response.content();

        System.out.println(embedding);
    }
}
