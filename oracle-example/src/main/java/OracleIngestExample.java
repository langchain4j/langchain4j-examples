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
import java.sql.SQLException;
import java.util.List;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Demonstrate using the OracleEmbeddingStore with the following
 * components to ingest the documents and perform a vector search.
 * OracleDocumentLoader to load the documents
 * OracleDocumentSplitter to split the text
 * OracleEmbeddingModel to get the vector embeddings
 * OracleEmbeddingStore to store the vector embeddings
 */
public class OracleIngestExample {

    public static void main(String[] args) throws SQLException, IOException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(System.getenv("ORACLE_JDBC_URL"));
        pds.setUser(System.getenv("ORACLE_JDBC_USER"));
        pds.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        Connection conn = pds.getConnection();

        String embedderPref = "{\"provider\": \"database\", \"model\": \"" + System.getenv("DEMO_ONNX_MODEL") + "\"}";
        String splitterPref = "{\"by\": \"chars\", \"max\": 50}";

        OracleDocumentLoader loader = new OracleDocumentLoader(conn);
        OracleEmbeddingModel embedder = new OracleEmbeddingModel(conn, embedderPref);
        OracleDocumentSplitter splitter = new OracleDocumentSplitter(conn, splitterPref);

        // column names for the output table
        String tableName = "TEST";
        String idColumn = "ID";
        String embeddingColumn = "EMBEDDING";
        String textColumn = "TEXT";
        String metadataColumn = "METADATA";

        // The call to build() should create a table with the configured names
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

        OracleEmbeddingModel.loadOnnxModel(
                conn,
                System.getenv("DEMO_ONNX_DIR"),
                System.getenv("DEMO_ONNX_FILE"),
                System.getenv("DEMO_ONNX_MODEL"));

        String loaderPref = "{\"file\": \"" + System.getenv("DEMO_FILE") + "\"}";
        List<Document> docs = loader.loadDocuments(loaderPref);

        // ingest the documents with the following components
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embedder)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(docs);
        
        // get the question from the user
        String question = "What is a database?";

        // get the vector representation
        Embedding questionAsVector = embedder.embed(question).content();

        // perform the vector search
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(questionAsVector)
                        .build()
        );

        // display the results
        List<EmbeddingMatch<TextSegment>> results = result.matches();
        for (EmbeddingMatch<TextSegment> match : results) {
            System.out.println("Score: " + match.score());
            System.out.println("Text Segment: " + match.embedded().text());
        }
    }
}
