import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.voyage.VoyageEmbeddingModel;
import dev.langchain4j.model.voyage.VoyageEmbeddingModelName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class VoyageEmbeddingModelExample {

    @Test
    void should_embed_single_text() {

        EmbeddingModel model = VoyageEmbeddingModel.withApiKey(System.getenv("VOYAGE_API_KEY"));
        System.out.println(model.embed("Hello World"));
    }

    @Test
    void should_respect_encoding_format() {

        // Using base64 encoding format to compress the embedding
        EmbeddingModel model = VoyageEmbeddingModel.builder()
                .apiKey(System.getenv("VOYAGE_API_KEY"))
                .modelName(VoyageEmbeddingModelName.VOYAGE_3_LITE)
                .timeout(Duration.ofSeconds(60))
                .encodingFormat("base64")
                .logRequests(true)
                .logResponses(true)
                .build();

        System.out.println(model.embed("Hello World"));
    }

    @Test
    void should_embed_multiple_segments() {

        EmbeddingModel model = VoyageEmbeddingModel.builder()
                .apiKey(System.getenv("VOYAGE_API_KEY"))
                .modelName(VoyageEmbeddingModelName.VOYAGE_3_LITE)
                .timeout(Duration.ofSeconds(60))
                .inputType("query")
                .logRequests(true)
                .logResponses(true)
                .build();

        TextSegment segment1 = TextSegment.from("hello");
        TextSegment segment2 = TextSegment.from("hi");

        System.out.println(model.embedAll(asList(segment1, segment2)));
    }

    @Test
    void should_embed_any_number_of_segments() {

        // given
        EmbeddingModel model = VoyageEmbeddingModel.builder()
                .apiKey(System.getenv("VOYAGE_API_KEY"))
                .modelName(VoyageEmbeddingModelName.VOYAGE_3_LITE)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        List<TextSegment> segments = new ArrayList<>();
        int segmentCount = 97;
        for (int i = 0; i < segmentCount; i++) {
            segments.add(TextSegment.from("text"));
        }

        // when
        System.out.println(model.embedAll(segments));
    }
}
