package devoxx;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.InMemoryEmbeddingStore;
import devoxx.model.Talk;
import org.jetbrains.annotations.NotNull;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static java.time.Duration.ofSeconds;
import static java.util.stream.Collectors.toList;

public class Test {

    public static void main(String[] args) throws IOException {

        List<Talk> talks = loadTalks();
        System.out.println("Talks loaded: " + talks.size());

//        EmbeddingModel embeddingModel = HuggingFaceEmbeddingModel.builder()
//                .accessToken(System.getenv("HF_API_KEY"))
//                .timeout(ofSeconds(60))
//                .build();

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .timeout(ofSeconds(60))
                .build();

        EmbeddingStore<Talk> embeddingStore = createEmbeddingStore(talks, embeddingModel);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("User: ");
            String query = br.readLine();

            if ("exit".equalsIgnoreCase(query)) {
                return;
            }

//            long embeddingStart = System.currentTimeMillis();
            Embedding queryEmbedding = embeddingModel.embed(query).get();
//            long embeddingStop = System.currentTimeMillis();
//            System.out.println("Embedding the query took " + (embeddingStop - embeddingStart) + " ms");

//            long searchStart = System.currentTimeMillis();
            List<EmbeddingMatch<Talk>> relevant = embeddingStore.findRelevant(queryEmbedding, 5);
//            long searchStop = System.currentTimeMillis();
//            System.out.println("Search took " + (searchStop - searchStart) + " ms");

            System.out.println("Results:");
            relevant.forEach(it -> {
                System.out.println();
                System.out.println("Title: " + it.embedded().getTitle());
                System.out.println("Description: " + it.embedded().getDescription());
                System.out.println("Speaker: " + it.embedded().getSpeakers());
                System.out.println("Score: " + it.score());
            });
        }
    }

    private static EmbeddingStore<Talk> createEmbeddingStore(List<Talk> talks, EmbeddingModel embeddingModel) {
        List<TextSegment> segments = talks.stream()
                .map(talk -> TextSegment.from(talk.toString()))
                .collect(toList());

//        long embeddingStart = System.currentTimeMillis();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).get();
//        long embeddingStop = System.currentTimeMillis();
//        System.out.println("Embedding all talks took " + (embeddingStop - embeddingStart) + " ms");

        EmbeddingStore<Talk> store = new InMemoryEmbeddingStore<>();
        for (int i = 0; i < 1000; i++) {// TODO
            store.addAll(embeddings, talks);
        }
        return store;
    }

    @NotNull
    private static List<Talk> loadTalks() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dvbe22.cfp.dev")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DevoxxApi api = retrofit.create(DevoxxApi.class);

        return api.talks().execute().body();
    }
}
