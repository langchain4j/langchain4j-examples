import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.document.DocumentSegment;
import dev.langchain4j.data.document.splitter.SentenceSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.langchain4j.data.document.DocumentType.TEXT;

public class EmbeddingsTest {

    public static void main(String[] args) {


        DocumentLoader documentLoader = DocumentLoader.from(toPath("test.txt"), TEXT);
        Document document = documentLoader.load();

        SentenceSplitter splitter = new SentenceSplitter();

        List<DocumentSegment> documentSegments = splitter.split(document);
//        System.out.println(documentSegments);

        String apiKey = System.getenv("HF_API_KEY");
        EmbeddingModel embeddingModel = HuggingFaceEmbeddingModel.withAccessToken(apiKey);

        List<Embedding> embeddings = embeddingModel.embedAll(documentSegments).get();


        List<Embedding> currentSegment = new ArrayList<>();
        currentSegment.add(embeddings.get(0));

        List<Float> similarityWithPrevious = new ArrayList<>();
        List<Float> similarityWithAllPrevious = new ArrayList<>();

        for (int i = 1; i < embeddings.size(); i++) {
            similarityWithPrevious.add(cosineSimilarity(embeddings.get(i - 1), embeddings.get(i)));
            similarityWithAllPrevious.add(cosineSimilarity(average(currentSegment), embeddings.get(i)));
            currentSegment.add(embeddings.get(i));
        }

        similarityWithPrevious.forEach(System.out::println);

        System.out.println();
        System.out.println();

        similarityWithAllPrevious.forEach(System.out::println);

        System.out.println();
        System.out.println();

        AtomicInteger ai = new AtomicInteger(0);
        documentSegments.forEach(it -> {
            int i = ai.get();
            System.out.println(i + ": [" + it.text() + "]");
            System.out.println(similarityWithPrevious.get(i) + " - " + similarityWithAllPrevious.get(i));
            ai.incrementAndGet();
        });
    }

    public static Embedding average(List<Embedding> embeddings) {

        int dimension = embeddings.get(0).vector().length;
        float[] averageVector = new float[dimension];

        // Sum up all vectors
        for (Embedding embedding : embeddings) {
            float[] vector = embedding.vector();
            for (int i = 0; i < dimension; i++) {
                averageVector[i] += vector[i];
            }
        }

        // Divide by the number of embeddings to get the average
        for (int i = 0; i < dimension; i++) {
            averageVector[i] /= embeddings.size();
        }

        return new Embedding(averageVector);
    }

    private static float cosineSimilarity(Embedding first, Embedding second) {
        float dot = 0.0F;
        float nru = 0.0F;
        float nrv = 0.0F;

        for (int i = 0; i < first.vector().length; ++i) {
            dot += first.vector()[i] * second.vector()[i];
            nru += first.vector()[i] * first.vector()[i];
            nrv += second.vector()[i] * second.vector()[i];
        }

        return dot / (float) (Math.sqrt((double) nru) * Math.sqrt((double) nrv));
    }

    private static Path toPath(String fileName) {
        try {
            URL fileUrl = ChatWithDocumentsExamples.class.getResource(fileName);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
