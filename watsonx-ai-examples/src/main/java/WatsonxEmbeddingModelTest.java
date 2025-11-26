import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.watsonx.WatsonxEmbeddingModel;

public class WatsonxEmbeddingModelTest {

    public static void main(String... args) {

        try {

            EmbeddingModel model = WatsonxEmbeddingModel.builder()
                .baseUrl(System.getenv("WATSONX_URL"))
                .apiKey(System.getenv("WATSONX_API_KEY"))
                .projectId(System.getenv("WATSONX_PROJECT_ID"))
                .modelName("ibm/granite-embedding-278m-multilingual")
                .build();

            System.out.println(model.embed("Hello from watsonx.ai"));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
