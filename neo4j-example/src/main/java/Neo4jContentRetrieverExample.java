import dev.langchain4j.community.rag.content.retriever.neo4j.Neo4jGraph;
import dev.langchain4j.community.rag.content.retriever.neo4j.Neo4jText2CypherRetriever;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.testcontainers.containers.Neo4jContainer;

import java.util.List;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class Neo4jContentRetrieverExample {
    // You can use "demo" api key for demonstration purposes.
    // You can get your own OpenAI API key here: https://platform.openai.com/account/api-keys
    public static final String OPENAI_API_KEY = getOrDefault(System.getenv("OPENAI_API_KEY"), "demo");
    public static final String OPENAI_BASE_URL = getOrDefault(System.getenv("OPENAI_BASE_URL"), "http://langchain4j.dev/demo/openai/v1");

    public static void main(String[] args) {
        final OpenAiChatModel chatLanguageModel = OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY)
                .baseUrl(OPENAI_BASE_URL)
                .modelName(GPT_4_O_MINI)
                .build();

        try (Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:5.26")
                .withoutAuthentication()
                .withLabsPlugins("apoc")) {
            neo4jContainer.start();
            try (Driver driver = GraphDatabase.driver(neo4jContainer.getBoltUrl(), AuthTokens.none())) {
                try (Neo4jGraph graph = Neo4jGraph.builder().driver(driver).build()) {
                    try (Session session = driver.session()) {
                        session.run("CREATE (book:Book {title: 'Dune'})<-[:WROTE]-(author:Person {name: 'Frank Herbert'})");
                    }
                    // The refreshSchema is needed only if we execute write operation after the `Neo4jGraph` instance, 
                    // in this case `CREATE (book:Book...`
                    // If CREATE (and in general write operations to the db) are performed externally before Neo4jGraph.builder(), 
                    // the refreshSchema() is not needed
                    graph.refreshSchema();

                    Neo4jText2CypherRetriever retriever = Neo4jText2CypherRetriever.builder()
                            .graph(graph)
                            .chatModel(chatLanguageModel)
                            .build();

                    Query query = new Query("Who is the author of the book 'Dune'?");

                    List<Content> contents = retriever.retrieve(query);

                    System.out.println(contents.get(0).textSegment().text()); // "Frank Herbert"
                }
            }
        }
    }
}
