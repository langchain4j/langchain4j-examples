import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.oracle.OracleDocumentLoader;
import dev.langchain4j.model.oracle.OracleSummaryLanguageModel;
import dev.langchain4j.model.output.Response;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Demonstrate summarizing a document. You can customize which provider to use
 * such as database for extracting a summary with Oracle Text or with a
 * third-party provider.
 * 
 * Define the following environment variables before running
 * ORACLE_JDBC_URL
 * ORACLE_JDBC_USER
 * ORACLE_JDBC_PASSWORD
 * DEMO_FILE
 * DEMO_CREDENTIAL
 */
public class OracleSummaryLanguageModelExample {

    public static void main(String[] args) throws SQLException, IOException {
        databaseSummary();
        thirdPartySummary();
    }

    public static void databaseSummary() throws SQLException, IOException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(System.getenv("ORACLE_JDBC_URL"));
        pds.setUser(System.getenv("ORACLE_JDBC_USER"));
        pds.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        Connection conn = pds.getConnection();

        String loadPref = "{\"file\": \"" + System.getenv("DEMO_FILE") + "\"}";
        String summaryPref = "{\"provider\": \"database\", \"gLevel\": \"S\"}";

        OracleDocumentLoader loader = new OracleDocumentLoader(conn);
        OracleSummaryLanguageModel model = new OracleSummaryLanguageModel(conn, summaryPref);

        List<Document> docs = loader.loadDocuments(loadPref);
        for (Document doc : docs) {
            Response<String> resp = model.generate(doc.text());
            System.out.println("summary=" + resp.content());
        }
    }
    
    public static void thirdPartySummary() throws SQLException, IOException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(System.getenv("ORACLE_JDBC_URL"));
        pds.setUser(System.getenv("ORACLE_JDBC_USER"));
        pds.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        Connection conn = pds.getConnection();

        // For a third-party provider, remember to create a credential
        // with dbms_vector.create_credential() and then refer to it here
        String loadPref = "{\"file\": \"" + System.getenv("DEMO_FILE") + "\"}";
        String summaryPref = "{\n"
                + "  \"provider\": \"ocigenai\",\n"
                + "  \"credential_name\": \"" + System.getenv("DEMO_CREDENTIAL") + "\",\n"
                + "  \"url\": \"https://inference.generativeai.us-chicago-1.oci.oraclecloud.com/20231130/actions/chat\",\n"
                + "  \"model\": \"cohere.command-r-08-2024\",\n"
                + "}";
        String proxy = System.getenv("DEMO_PROXY");

        OracleDocumentLoader loader = new OracleDocumentLoader(conn);
        OracleSummaryLanguageModel model = new OracleSummaryLanguageModel(conn, summaryPref, proxy);

        List<Document> docs = loader.loadDocuments(loadPref);
        for (Document doc : docs) {
            Response<String> resp = model.generate(doc.text());
            System.out.println("summary=" + resp.content());
        }
    }
}
