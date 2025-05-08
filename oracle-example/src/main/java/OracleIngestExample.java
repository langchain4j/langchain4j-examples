import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.oracle.OracleDocumentLoader;
import dev.langchain4j.data.document.splitter.oracle.OracleDocumentSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.oracle.OracleEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import static dev.langchain4j.store.embedding.oracle.CreateOption.CREATE_OR_REPLACE;
import dev.langchain4j.store.embedding.oracle.EmbeddingTable;
import dev.langchain4j.store.embedding.oracle.OracleEmbeddingStore;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Demonstrate how to use low-level LangChain4j APIs to load the documents,
 * split the text, and get the vector embeddings or the OracleEmbeddingStore
 * to hide the manual steps for ingesting documents into an embedding store
 * for search/retrieval.
 *
 * The following components are used:
 * OracleDocumentLoader to load the documents
 * OracleDocumentSplitter to split the text
 * OracleEmbeddingModel to get the vector embeddings
 * OracleEmbeddingStore to store the vector embeddings
 * 
 * Define the following environment variables before running
 * ORACLE_JDBC_URL
 * ORACLE_JDBC_USER
 * ORACLE_JDBC_PASSWORD
 * DEMO_ONNX_DIR
 * DEMO_ONNX_FILE
 * DEMO_ONNX_MODEL
 * DEMO_FILE
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

        // Load the document that includes the information you'd like to "chat" about with the model.
        String loaderPref = "{\"file\": \"" + System.getenv("DEMO_FILE") + "\"}";
        OracleDocumentLoader loader = new OracleDocumentLoader(conn);
        List<Document> docs = loader.loadDocuments(loaderPref);

        // Split document into segments 100 tokens each
        String splitterPref = "{\"by\": \"words\", \"max\": 100}";
        OracleDocumentSplitter splitter = new OracleDocumentSplitter(conn, splitterPref);
        List<TextSegment> segments = null;
        for (Document doc : docs) {
            segments = splitter.split(doc);
            for (TextSegment segment : segments) {
                System.out.println("segment=" + segment.text());
            }
        }

        // Embed segments (convert them into vectors that represent the meaning) using embedding model
        String embedderPref = "{\"provider\": \"database\", \"model\": \"" + System.getenv("DEMO_ONNX_MODEL") + "\"}";
        OracleEmbeddingModel embeddingModel = new OracleEmbeddingModel(conn, embedderPref);
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        for (Embedding embedding : embeddings) {
            System.out.println("embedding=" + embedding);
        }

        // set column names for the output table
        String tableName = "TEST";
        String idColumn = "ID";
        String embeddingColumn = "EMBEDDING";
        String textColumn = "TEXT";
        String metadataColumn = "METADATA";

        // setup the embedding store
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
        embeddingStore.addAll(embeddings, segments);

        // Specify the question you want to ask the model
        String question = "Who is Charlie?";

        // Embed the question
        Embedding questionEmbedding = embeddingModel.embed(question).content();

        // Find relevant embeddings in embedding store by semantic similarity
        // You can play with parameters below to find a sweet spot for your specific use case
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
            System.out.println("Text Segment: " + match.embedded().text());
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

        // set the loader, splitter, and embedding preferences
        String loaderPref = "{\"file\": \"" + System.getenv("DEMO_FILE") + "\"}";
        String splitterPref = "{\"by\": \"words\", \"max\": 100}";
        String embedderPref = "{\"provider\": \"database\", \"model\": \"" + System.getenv("DEMO_ONNX_MODEL") + "\"}";

        OracleDocumentLoader loader = new OracleDocumentLoader(conn);
        OracleDocumentSplitter splitter = new OracleDocumentSplitter(conn, splitterPref);
        OracleEmbeddingModel embedder = new OracleEmbeddingModel(conn, embedderPref);

        // load the ONNX model for embedding
        OracleEmbeddingModel.loadOnnxModel(
                conn,
                System.getenv("DEMO_ONNX_DIR"),
                System.getenv("DEMO_ONNX_FILE"),
                System.getenv("DEMO_ONNX_MODEL"));

        // load the document
        List<Document> docs = loader.loadDocuments(loaderPref);

        // set column names for the output table
        String tableName = "TEST";
        String idColumn = "ID";
        String embeddingColumn = "EMBEDDING";
        String textColumn = "TEXT";
        String metadataColumn = "METADATA";

        // setup the embedding store
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

        // ingest the documents with the following components
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embedder)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(docs);

        // check the chunks
        System.out.println("chunks inserted:");
        String queryEmbeddingStore = "select * from %s".formatted(tableName);
        try (PreparedStatement stmt = conn.prepareStatement(queryEmbeddingStore)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString(idColumn);
                    String text = rs.getString(textColumn);

                    String ending = text.length() > 50 ? "..." : "";
                    System.out.println(id + "\t" + text.substring(0, 50) + ending);
                }
            }
        }

        // get the question
        String question = "Who is Charlie?";

        // get the vector representation
        Embedding questionAsVector = embedder.embed(question).content();

        // perform the vector search
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(questionAsVector)
                        .build()
        );

        // display the results
        System.out.println(question);
        List<EmbeddingMatch<TextSegment>> results = result.matches();
        for (EmbeddingMatch<TextSegment> match : results) {
            System.out.println("Score: " + match.score());
            System.out.println("Text Segment: " + match.embedded().text());
        }
    }
}
