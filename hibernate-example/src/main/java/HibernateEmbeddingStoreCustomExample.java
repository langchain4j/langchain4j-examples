import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.SchemaToolingSettings;
import org.hibernate.query.restriction.Restriction;
import org.hibernate.tool.schema.Action;
import org.hibernate.tool.schema.SourceType;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.hibernate.HibernateEmbeddingStore;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class HibernateEmbeddingStoreCustomExample {

    public static void main(String[] args) {

        DockerImageName dockerImageName = DockerImageName.parse("pgvector/pgvector:pg16");

        Configuration configuration = new Configuration()
                .addAnnotatedClass(Book.class)
                .setSchemaExportAction(Action.CREATE_DROP)
                .setProperty(SchemaToolingSettings.JAKARTA_HBM2DDL_CREATE_SOURCE, SourceType.SCRIPT_THEN_METADATA)
                .setProperty(SchemaToolingSettings.JAKARTA_HBM2DDL_CREATE_SCRIPT_SOURCE, "/setup.sql")
                .showSql( true, true, true );
        try (PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(dockerImageName)) {
            postgreSQLContainer.start();
            try (SessionFactory factory = configuration.setJdbcUrl(postgreSQLContainer.getJdbcUrl())
                     .setCredentials(postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword())
                     .buildSessionFactory()) {

                // 1. Insert base data
                factory.inStatelessTransaction( session -> {
                    session.insertMultiple(List.of(
                            new Book(
                                    "123456789",
                                    "Hibernate ABC",
                                    "/hibernate1.txt",
                                    "English",
                                    "java", "database", "sql"
                            ),
                            new Book(
                                    "234567891",
                                    "Hibernation of animals",
                                    "/hibernate2.txt",
                                    "English",
                                    "animal", "hibernation"
                            ),
                            new Book(
                                    "345678912",
                                    "Hibernate ORM",
                                    "/hibernate3.txt",
                                    "German",
                                    "java", "database", "sql"
                            )
                    ));
                } );

                EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

                // 2. Create an embedding store for the entity
                HibernateEmbeddingStore<Book> embeddingStore = HibernateEmbeddingStore.builder(Book.class)
                        .sessionFactory(factory)
                        .build();

                // 3. Indexing process that fills embeddings in batches
                factory.inStatelessTransaction( session -> {
                    List<Book> books = session.createSelectionQuery("from Book b where b.embedding is null", Book.class)
                            .setMaxResults(50)
                            .getResultList();
                    for (Book book : books) {
                        // Use a DocumentParser that can handle the content of the file
                        DocumentParser documentParser = new TextDocumentParser();
                        Document document = documentParser.parse(HibernateEmbeddingStoreCustomExample.class.getResourceAsStream(book.getFileName()));

                        // Set the embedding on the entity
                        Response<Embedding> response = embeddingModel.embed(document.toTextSegment());
                        book.setEmbedding(response.content().vector());
                        book.setMetadata(response.metadata());
                    }
                    // Update entities
                    session.updateMultiple(books);
                } );

                // 4. Create an embedding for a query
                Embedding queryEmbedding = embeddingModel.embed("What is a good book about Hibernate ORM?").content();

                // 5. Query the EmbeddingStore
                EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(1)
                        .build();

                List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.search(embeddingSearchRequest).matches();

                EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

                System.out.println(embeddingMatch.score()); // 0.8524735973022499
                System.out.println(embeddingMatch.embeddingId()); // 123456789

                // 6. Hybrid search with Hibernate ORM Restriction API
                relevant = embeddingStore.search(
                        queryEmbedding,
                        Restriction.equal(Book_.language, "English")
                ).matches();

                embeddingMatch = relevant.get(0);

                System.out.println(embeddingMatch.score()); // 0.8524735973022499
                System.out.println(embeddingMatch.embeddingId()); // 123456789
            }
        }
    }
}
