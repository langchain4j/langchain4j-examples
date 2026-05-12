import java.util.List;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.model.watsonx.WatsonxScoringModel;

public class WatsonxScoringModelTest {

    public static void main(String... args) {

        try {

            ScoringModel model = WatsonxScoringModel.builder()
                .baseUrl(System.getenv("WATSONX_URL"))
                .apiKey(System.getenv("WATSONX_API_KEY"))
                .projectId(System.getenv("WATSONX_PROJECT_ID"))
                .modelName("cross-encoder/ms-marco-minilm-l-12-v2")
                .build();
                
            System.out.println(model.scoreAll(List.of(TextSegment.from("Example_1"), TextSegment.from("Example_2")), "Hello from watsonx.ai"));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
