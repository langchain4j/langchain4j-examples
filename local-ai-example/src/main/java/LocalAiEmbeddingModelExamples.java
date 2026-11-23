import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.localai.LocalAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.testcontainers.shaded.com.google.common.collect.Lists;

import java.util.List;

public class LocalAiEmbeddingModelExamples extends AbstractLocalAiInfrastructure {

    static EmbeddingModel embeddingModel = LocalAiEmbeddingModel.builder()
            .baseUrl(localAi.getBaseUrl())
            .modelName("ggml-model-q4_0")
            .logRequests(true)
            .logResponses(true)
            .build();

    static class Simple_Embed {
        public static void main(String[] args) {
            Response<Embedding> response = embeddingModel.embed("better go home and weave a net than to stand by the pond longing for fish.");

            System.out.println(response.content());
        }
    }

    static class List_Embed {
        public static void main(String[] args) {
            TextSegment textSegment1 = TextSegment.from("better go home and weave a net than ");
            TextSegment textSegment2 = TextSegment.from("to stand by the pond longing for fish.");
            Response<List<Embedding>> listResponse = embeddingModel.embedAll(Lists.newArrayList(textSegment1, textSegment2));
            
            listResponse.content().stream().map(Embedding::dimension).forEach(System.out::println);
        }
    }

}
