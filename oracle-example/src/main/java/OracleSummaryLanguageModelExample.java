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

public class OracleSummaryLanguageModelExample {

    public static void main(String[] args) throws SQLException, IOException {
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
}
