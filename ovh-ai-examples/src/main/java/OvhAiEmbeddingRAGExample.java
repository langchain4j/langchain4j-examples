import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ovhai.OvhAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.nio.file.Paths;
import java.util.List;

public class OvhAiEmbeddingRAGExample {

    public static void main(String[] args) throws Exception {
        Document document = loadDocument(
            Paths.get(OvhAiEmbeddingRAGExample.class.getResource("story-about-happy-carrot.txt").toURI()),
            new TextDocumentParser()
        );

        DocumentSplitter splitter = DocumentSplitters.recursive(200, 0);
        List<TextSegment> segments = splitter.split(document);

        EmbeddingModel embeddingModel = OvhAiEmbeddingModel.withApiKey(System.getenv("OVH_AI_API_KEY"));

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        for (TextSegment segment : segments) {
            embeddingStore.add(embeddingModel.embed(segment).content(), segment);
        }
        String question = "Charlie";
        Embedding questionEmbedding = embeddingModel.embed(question).content();

        int maxResults = 3;
        double minScore = 0.7;
        EmbeddingSearchResult<TextSegment> relevantEmbeddings = embeddingStore.search(
            new EmbeddingSearchRequest(questionEmbedding, maxResults, minScore, null)
        );

        relevantEmbeddings
            .matches()
            .forEach(match -> {
                System.out.println(match.embedded().text());
            });
    }
}
