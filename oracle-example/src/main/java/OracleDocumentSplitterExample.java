import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.oracle.OracleDocumentLoader;
import dev.langchain4j.data.document.splitter.oracle.OracleDocumentSplitter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

public class OracleDocumentSplitterExample {

    public static void main(String[] args) throws SQLException, IOException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(System.getenv("ORACLE_JDBC_URL"));
        pds.setUser(System.getenv("ORACLE_JDBC_USER"));
        pds.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        Connection conn = pds.getConnection();

        String loadPref = "{\"file\": \"" + System.getenv("DEMO_FILE") + "\"}";
        String splitPref = "{\"by\": \"chars\", \"max\": 50}";

        OracleDocumentLoader loader = new OracleDocumentLoader(conn);
        OracleDocumentSplitter splitter = new OracleDocumentSplitter(conn, splitPref);

        List<Document> docs = loader.loadDocuments(loadPref);
        for (Document doc : docs) {
            String[] chunks = splitter.split(doc.text());
            for (String chunk : chunks) {
                System.out.println("chunk=" + chunk);
            }
        }
    }
}
