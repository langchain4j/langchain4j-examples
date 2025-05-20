import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.oracle.OracleDocumentLoader;
import dev.langchain4j.data.document.splitter.oracle.OracleDocumentSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.oracle.OracleEmbeddingModel;
import dev.langchain4j.model.oracle.OracleSummaryLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import static dev.langchain4j.store.embedding.oracle.CreateOption.CREATE_OR_REPLACE;
import dev.langchain4j.store.embedding.oracle.EmbeddingTable;
import dev.langchain4j.store.embedding.oracle.OracleEmbeddingStore;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Demonstrate how to ingest documents either by using the low-level LangChain4j
 * APIs to load the documents, split the text, and get the vector embeddings or
 * the OracleEmbeddingStore to hide the manual steps of ingesting into an
 * embedding store for search/retrieval.
 *
 * This example requires the following environment variables:
 * ORACLE_JDBC_URL
 * ORACLE_JDBC_USER
 * ORACLE_JDBC_PASSWORD
 * DEMO_ONNX_DIR
 * DEMO_ONNX_FILE
 * DEMO_ONNX_MODEL
 * DEMO_DIRECTORY
 */
public class OracleIngestExample {

    public static void main(String[] args) throws SQLException, IOException {
        lowLevelExample();
        ingestExample();
    }

    /**
     * This example demonstrates how to use low-level LangChain4j APIs to load
     * the documents, split the text, and get the vector embeddings for
     * search/retrieval.
     */
    public static void lowLevelExample() throws SQLException, IOException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(System.getenv("ORACLE_JDBC_URL"));
        pds.setUser(System.getenv("ORACLE_JDBC_USER"));
        pds.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        Connection conn = pds.getConnection();

        // load the ONNX model for embedding
        OracleEmbeddingModel.loadOnnxModel(
                conn,
                System.getenv("DEMO_ONNX_DIR"),
                System.getenv("DEMO_ONNX_FILE"),
                System.getenv("DEMO_ONNX_MODEL"));
        
        // set the loader, splitter, embedding, and summary model preferences
        String loaderPref = "{\"dir\": \"" + System.getenv("DEMO_DIRECTORY") + "\"}";
        String splitterPref = "{\"by\": \"words\", \"max\": 100}";
        String embedderPref = "{\"provider\": \"database\", \"model\": \"" + System.getenv("DEMO_ONNX_MODEL") + "\"}";
        String summaryPref = "{\"provider\": \"database\", \"glevel\": \"S\"}";

        OracleDocumentLoader loader = new OracleDocumentLoader(conn);
        OracleDocumentSplitter splitter = new OracleDocumentSplitter(conn, splitterPref);
        OracleEmbeddingModel embeddingModel = new OracleEmbeddingModel(conn, embedderPref);
        OracleSummaryLanguageModel summaryModel = new OracleSummaryLanguageModel(conn, summaryPref);

        // Load the documents with the information that you would like to search on
        List<Document> docs = loader.loadDocuments(loaderPref);

        // Split document into segments
        List<TextSegment> allSegments = new ArrayList<>();
        for (Document doc : docs) {          
            // For each doc, add a summary to the metadata
            // This will get copied into each segment
            Response<String> resp = summaryModel.generate(doc.text());
            doc.metadata().put("summary", resp.content());

            List<TextSegment> segments = splitter.split(doc);
            allSegments.addAll(segments);
        }

        // Embed segments (convert them into vectors that represent the meaning) using embedding model
        List<Embedding> embeddings = embeddingModel.embedAll(allSegments).content();

        // setup the embedding store
        
        // set column names for the output table
        String tableName = "TEST";
        String idColumn = "ID";
        String embeddingColumn = "EMBEDDING";
        String textColumn = "TEXT";
        String metadataColumn = "METADATA";

        // build() should create a table with the configured names
        OracleEmbeddingStore embeddingStore = OracleEmbeddingStore.builder()
                .dataSource(pds)
                .embeddingTable(EmbeddingTable.builder()
                        .createOption(CREATE_OR_REPLACE)
                        .name(tableName)
                        .idColumn(idColumn)
                        .embeddingColumn(embeddingColumn)
                        .textColumn(textColumn)
                        .metadataColumn(metadataColumn)
                        .build())
                .build();

        // Store embeddings into embedding store for further search / retrieval
        embeddingStore.addAll(embeddings, allSegments);

        // Get the question
        String question = "Who is John Doe?";

        // Embed the question
        Embedding questionEmbedding = embeddingModel.embed(question).content();

        // Find relevant embeddings in embedding store by semantic similarity
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(questionEmbedding)
                .maxResults(3)
                .minScore(0.6)
                .build();
        List<EmbeddingMatch<TextSegment>> relevantEmbeddings = embeddingStore.search(embeddingSearchRequest).matches();

        // display the results
        System.out.println(question);
        for (EmbeddingMatch<TextSegment> match : relevantEmbeddings) {
            System.out.println("Score: " + match.score());
            System.out.println("Segment: " + match.embedded().text());
            System.out.println("Metadata: " + match.embedded().metadata());
        }
    }

    /**
     * This example demonstrates how to use the EmbeddingStoreIngestor to hide
     * manual steps of ingesting documents for search/retrieval.
     */
    public static void ingestExample() throws SQLException, IOException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(System.getenv("ORACLE_JDBC_URL"));
        pds.setUser(System.getenv("ORACLE_JDBC_USER"));
        pds.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        Connection conn = pds.getConnection();

        // load the ONNX model for embedding
        OracleEmbeddingModel.loadOnnxModel(
                conn,
                System.getenv("DEMO_ONNX_DIR"),
                System.getenv("DEMO_ONNX_FILE"),
                System.getenv("DEMO_ONNX_MODEL"));

        // set the loader, splitter, and embedding preferences
        String loaderPref = "{\"dir\": \"" + System.getenv("DEMO_DIRECTORY") + "\"}";
        String splitterPref = "{\"by\": \"words\", \"max\": 100}";
        String embedderPref = "{\"provider\": \"database\", \"model\": \"" + System.getenv("DEMO_ONNX_MODEL") + "\"}";

        OracleDocumentLoader loader = new OracleDocumentLoader(conn);
        OracleDocumentSplitter splitter = new OracleDocumentSplitter(conn, splitterPref);
        OracleEmbeddingModel embeddingModel = new OracleEmbeddingModel(conn, embedderPref);

        // setup the embedding store
        
        // set column names for the output table
        String tableName = "TEST";
        String idColumn = "ID";
        String embeddingColumn = "EMBEDDING";
        String textColumn = "TEXT";
        String metadataColumn = "METADATA";

        // build() should create a table with the configured names
        OracleEmbeddingStore embeddingStore = OracleEmbeddingStore.builder()
                .dataSource(pds)
                .embeddingTable(EmbeddingTable.builder()
                        .createOption(CREATE_OR_REPLACE)
                        .name(tableName)
                        .idColumn(idColumn)
                        .embeddingColumn(embeddingColumn)
                        .textColumn(textColumn)
                        .metadataColumn(metadataColumn)
                        .build())
                .build();

        // build an ingestor with the following components
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        // load and ingest the documents
        // this will call the splitter to split into segments,
        // embedding model to get the embeddings, and then store the
        // embeddings into the embedding store for further search / retrieval
        List<Document> docs = loader.loadDocuments(loaderPref);
        ingestor.ingest(docs);

        // get the question
        String question = "Who is John Doe?";

        // get the vector representation
        Embedding questionAsVector = embeddingModel.embed(question).content();

        // perform the vector search
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(questionAsVector)
                        .maxResults(3)
                        .minScore(0.6)
                        .build()
        );

        // display the results
        System.out.println(question);
        List<EmbeddingMatch<TextSegment>> results = result.matches();
        for (EmbeddingMatch<TextSegment> match : results) {
            System.out.println("Score: " + match.score());
            System.out.println("Segment: " + match.embedded().text());
            System.out.println("Metadata: " + match.embedded().metadata());
        }
    }
}
