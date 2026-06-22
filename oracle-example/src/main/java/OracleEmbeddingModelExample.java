import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.oracle.OracleEmbeddingModel;
import dev.langchain4j.model.output.Response;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Demonstrate getting the vector embeddings. You can customize which provider
 * to use such as database for an ONNX model or with a third-party provider.
 * 
 * This example requires the following environment variables:
 * ORACLE_JDBC_URL
 * ORACLE_JDBC_USER
 * ORACLE_JDBC_PASSWORD
 * DEMO_ONNX_DIR
 * DEMO_ONNX_FILE
 * DEMO_ONNX_MODEL
 * DEMO_CREDENTIAL
 */
public class OracleEmbeddingModelExample {

    public static void main(String[] args) throws SQLException {
        databaseEmbeddings();
        thirdPartyEmbeddings();
    }
    
    public static void databaseEmbeddings() throws SQLException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(System.getenv("ORACLE_JDBC_URL"));
        pds.setUser(System.getenv("ORACLE_JDBC_USER"));
        pds.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        Connection conn = pds.getConnection();

        // load an ONNX model into the database
        // remember to create a directory alias with
        // create or replace directory MODEL_DIR as '/path/to/model';
        OracleEmbeddingModel.loadOnnxModel(
                conn,
                System.getenv("DEMO_ONNX_DIR"),
                System.getenv("DEMO_ONNX_FILE"),
                System.getenv("DEMO_ONNX_MODEL"));

        String pref = "{\"provider\": \"database\", \"model\": \"" + System.getenv("DEMO_ONNX_MODEL") + "\"}";

        OracleEmbeddingModel model = new OracleEmbeddingModel(conn, pref);

        // embed a single string
        Response<Embedding> response = model.embed("I love Java");
        Embedding embedding = response.content();
        System.out.println(embedding);

        // embed a list of text
        List<TextSegment> textSegments = new ArrayList<>();
        textSegments.add(TextSegment.from("I like soccer."));
        textSegments.add(TextSegment.from("I love Stephen King."));
        textSegments.add(TextSegment.from("The weather is good today."));
        Response<List<Embedding>> resp = model.embedAll(textSegments);
        System.out.println(resp.content());
    }
    
    public static void thirdPartyEmbeddings() throws SQLException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(System.getenv("ORACLE_JDBC_URL"));
        pds.setUser(System.getenv("ORACLE_JDBC_USER"));
        pds.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        Connection conn = pds.getConnection();

        // For a third-party provider, remember to create a credential
        // with dbms_vector.create_credential() and then refer to it here
        String pref = "{\n" +
            "  \"provider\": \"ocigenai\",\n" +
            "  \"credential_name\": \"" + System.getenv("DEMO_CREDENTIAL") + "\",\n" +
            "  \"url\": \"https://inference.generativeai.us-chicago-1.oci.oraclecloud.com/20231130/actions/embedText\",\n" +
            "  \"model\": \"cohere.embed-english-light-v3.0\"\n" +
            "}";

        OracleEmbeddingModel model = new OracleEmbeddingModel(conn, pref);

        // embed a single string
        Response<Embedding> response = model.embed("I love Java");
        Embedding embedding = response.content();
        System.out.println(embedding);

        // embed a list of text
        List<TextSegment> textSegments = new ArrayList<>();
        textSegments.add(TextSegment.from("I like soccer."));
        textSegments.add(TextSegment.from("I love Stephen King."));
        textSegments.add(TextSegment.from("The weather is good today."));
        Response<List<Embedding>> resp = model.embedAll(textSegments);
        System.out.println(resp.content());
    }
}
