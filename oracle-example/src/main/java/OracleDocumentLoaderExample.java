import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.oracle.DirectoryPreference;
import dev.langchain4j.data.document.loader.oracle.FilePreference;
import dev.langchain4j.data.document.loader.oracle.OracleDocumentLoader;
import dev.langchain4j.data.document.loader.oracle.TablePreference;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/**
 * Demonstrate loading documents from the file system or a table.
 * The documents can be in any format supported by the Oracle Text filter
 * including Word, PDF, HTML, and text files. If it is a rich text document
 * like Word or PDF, it will be converted into plain text and contain any
 * metadata associated with it.
 * 
 * This example requires the following environment variables:
 * ORACLE_JDBC_URL
 * ORACLE_JDBC_USER
 * ORACLE_JDBC_PASSWORD
 * DEMO_FILE
 * DEMO_DIRECTORY
 * DEMO_OWNER
 * DEMO_TABLE
 * DEMO_COLUMN
 */
public class OracleDocumentLoaderExample {

    public static void main(String[] args) throws SQLException, IOException {
        loadFromFile();
        loadFromDirectory();
        loadFromTable();
    }
    
    private static void loadFromFile() throws IOException, SQLException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(System.getenv("ORACLE_JDBC_URL"));
        pds.setUser(System.getenv("ORACLE_JDBC_USER"));
        pds.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        Connection conn = pds.getConnection();

        // Can build pref as a string
        // String pref = "{\"file\": \"...\"}";
        // Alternatively, can use FilePreference
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

    private static void loadFromDirectory() throws IOException, SQLException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(System.getenv("ORACLE_JDBC_URL"));
        pds.setUser(System.getenv("ORACLE_JDBC_USER"));
        pds.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        Connection conn = pds.getConnection();

        // Can build pref as a string
        // String pref = "{\"dir\": \"...\"}";
        // Alternatively, can use DirectoryPreference
        ObjectMapper mapper = new ObjectMapper();
        DirectoryPreference loaderPref = new DirectoryPreference();
        loaderPref.setDirectory(System.getenv("DEMO_DIRECTORY"));
        String pref = mapper.writeValueAsString(loaderPref);

        OracleDocumentLoader loader = new OracleDocumentLoader(conn);

        List<Document> docs = loader.loadDocuments(pref);
        for (Document doc : docs) {
            System.out.println("metadata=" + doc.metadata());
            System.out.println("text=" + doc.text());
        }
    }

    private static void loadFromTable() throws IOException, SQLException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(System.getenv("ORACLE_JDBC_URL"));
        pds.setUser(System.getenv("ORACLE_JDBC_USER"));
        pds.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        Connection conn = pds.getConnection();

        // Can build pref as a string
        // String pref = "{\"owner\": \"...\", \"tablename\": \"...\", \"colname\": \"...\"}";
        // Alternatively, can use TablePreference
        ObjectMapper mapper = new ObjectMapper();
        TablePreference loaderPref = new TablePreference();
        loaderPref.setOwner(System.getenv("DEMO_OWNER"));
        loaderPref.setTableName(System.getenv("DEMO_TABLE"));
        loaderPref.setColumnName(System.getenv("DEMO_COLUMN"));
        String pref = mapper.writeValueAsString(loaderPref);

        OracleDocumentLoader loader = new OracleDocumentLoader(conn);

        List<Document> docs = loader.loadDocuments(pref);
        for (Document doc : docs) {
            System.out.println("metadata=" + doc.metadata());
            System.out.println("text=" + doc.text());
        }
    }

}
