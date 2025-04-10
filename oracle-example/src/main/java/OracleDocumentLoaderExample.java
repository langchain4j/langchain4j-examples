import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.oracle.FilePreference;
import dev.langchain4j.data.document.loader.oracle.OracleDocumentLoader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

public class OracleDocumentLoaderExample {

    public static void main(String[] args) throws SQLException, IOException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(System.getenv("ORACLE_JDBC_URL"));
        pds.setUser(System.getenv("ORACLE_JDBC_USER"));
        pds.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        Connection conn = pds.getConnection();

        // Can build pref as a string
        // String pref = "{\"file\": \"" + System.getenv("DEMO_FILE") + "\"}";

        // Alternatively, can use FilePreference, DirectoryPreference, or TablePreference
        ObjectMapper mapper = new ObjectMapper();
        FilePreference loaderPref = new FilePreference();
        loaderPref.setFilename(System.getenv("DEMO_FILE"));
        String pref = mapper.writeValueAsString(loaderPref);

        OracleDocumentLoader loader = new OracleDocumentLoader(conn);

        List<Document> docs = loader.loadDocuments(pref);
        for (Document doc : docs) {
            System.out.println("metadata=" + doc.metadata());
            System.out.println("text=" + doc.text());
        }
    }
}
