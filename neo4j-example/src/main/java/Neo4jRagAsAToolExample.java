import dev.langchain4j.community.chain.RetrievalQAChain;
import dev.langchain4j.community.store.embedding.neo4j.Neo4jEmbeddingStoreIngestor;
import dev.langchain4j.community.store.embedding.neo4j.ParentChildGraphIngestor;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByRegexSplitter;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.AiServices;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.testcontainers.containers.Neo4jContainer;
import util.Utils;

import java.time.Duration;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class Neo4jRagAsAToolExample {

    public static class RagTools {

        private final Neo4jEmbeddingStoreIngestor ingestor;
        private final RetrievalQAChain qaChain;

        public RagTools(Neo4jEmbeddingStoreIngestor ingestor, RetrievalQAChain qaChain) {
            this.ingestor = ingestor;
            this.qaChain = qaChain;
        }

        @Tool("Ingest from document")
        public String ingest(String text) {
            Document document = FileSystemDocumentLoader.loadDocument(Utils.toPath(text));

            ingestor.ingest(document);
            return "Document ingested";
        }

        @Tool("Answer the question based only on the context provided from the ingested documents.")
        public String ask(String question) {
            return qaChain.execute(Query.from(question));
        }
    }

    public static void main(String[] args) {
        try (Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.26").withAdminPassword("pass1234")) {
            neo4j.start();
            // Setup OpenAI chat model
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .baseUrl(System.getenv("OPENAI_BASE_URL"))
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName(GPT_4_O_MINI)
                    .timeout(Duration.ofSeconds(60))
                    .build();
            final AllMiniLmL6V2QuantizedEmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

            // MainDoc splitter splits on paragraphs (double newlines)
            final String expectedQuery = "\\n\\n";
            int maxSegmentSize = 250;
            DocumentSplitter parentSplitter = new DocumentByRegexSplitter(expectedQuery, expectedQuery, maxSegmentSize, 0);

            // Child splitter splits on periods (sentences)
            final String expectedQueryChild = "\\. ";
            DocumentSplitter childSplitter =
                    new DocumentByRegexSplitter(expectedQueryChild, expectedQuery, maxSegmentSize, 0);

            final Driver driver = GraphDatabase.driver(neo4j.getBoltUrl(), AuthTokens.basic("neo4j", "pass1234"));

            Neo4jEmbeddingStoreIngestor ingestor = ParentChildGraphIngestor.builder()
                    .driver(driver)
                    .documentSplitter(parentSplitter)
                    .documentSplitter(childSplitter)
                    .embeddingModel(embeddingModel)
                    .build();

            // Retriever from Neo4j embeddings
            ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(ingestor.getEmbeddingStore())
                    .embeddingModel(embeddingModel)
                    .maxResults(5)
                    .minScore(0.4)
                    .build();

            // Retrieval QA chain using retriever and LLM
            RetrievalQAChain retrievalQAChain = RetrievalQAChain.builder()
                    .contentRetriever(retriever)
                    .chatModel(chatModel)
                    .build();


            RagTools tools = new RagTools(ingestor, retrievalQAChain);

            // Build assistant with ingestion tool and retrieval QA tool
            Utils.Assistant assistant = AiServices.builder(Utils.Assistant.class)
                    .tools(tools)
                    .chatModel(chatModel)
                    .build();


            // Ask a question answered by retrieval QA chain
            String chat = assistant.chat("""
                    Ingest from document 'myname.txt', and then return the answer for the question 'What is the cancellation policy?'""");
            System.out.println("ANSWER: " + chat);
            // example output:
            // `ANSWER: John Doe is a Super Saiyan`
        }
    }
}
