import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.model.voyageai.VoyageAiScoringModel;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static dev.langchain4j.model.voyageai.VoyageAiScoringModelName.RERANK_LITE_1;
import static java.util.Arrays.asList;

public class VoyageAiScoringModelExample {

    @Test
    void should_score_single_text() {

        ScoringModel model = VoyageAiScoringModel.builder()
                .apiKey(System.getenv("VOYAGE_API_KEY"))
                .modelName(RERANK_LITE_1)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        String text = "labrador retriever";
        String query = "tell me about dogs";

        System.out.println(model.score(text, query));
    }

    @Test
    void should_score_multiple_segments_with_all_parameters() {

        ScoringModel model = VoyageAiScoringModel.builder()
                .apiKey(System.getenv("VOYAGE_API_KEY"))
                .modelName(RERANK_LITE_1)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        TextSegment catSegment = TextSegment.from("The Maine Coon is a large domesticated cat breed.");
        TextSegment dogSegment = TextSegment.from("The sweet-faced, lovable Labrador Retriever is one of America's most popular dog breeds, year after year.");
        List<TextSegment> segments = asList(catSegment, dogSegment);

        String query = "tell me about dogs";

        System.out.println(model.scoreAll(segments, query));
    }

    @Test
    void should_respect_top_k() {

        ScoringModel model = VoyageAiScoringModel.builder()
                .apiKey(System.getenv("VOYAGE_API_KEY"))
                .modelName(RERANK_LITE_1)
                .timeout(Duration.ofSeconds(60))
                .topK(1)
                .logRequests(true)
                .logResponses(true)
                .build();

        TextSegment catSegment = TextSegment.from("The Maine Coon is a large domesticated cat breed.");
        TextSegment dogSegment = TextSegment.from("The sweet-faced, lovable Labrador Retriever is one of America's most popular dog breeds, year after year.");
        List<TextSegment> segments = asList(catSegment, dogSegment);

        String query = "tell me about dogs";

        // when
        System.out.println(model.scoreAll(segments, query));
    }
}
