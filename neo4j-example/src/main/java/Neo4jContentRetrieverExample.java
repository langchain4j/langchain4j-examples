import dev.langchain4j.community.rag.content.retriever.neo4j.Neo4jGraph;
import dev.langchain4j.community.rag.content.retriever.neo4j.Neo4jText2CypherRetriever;
import dev.langchain4j.model.chat.ChatModel;
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
    public static final String OPENAI_BASE_URL = "demo".equals(OPENAI_API_KEY) ? "http://langchain4j.dev/demo/openai/v1" : null;

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
                    contentRetrieverWithMinimalConfig(driver, graph, chatLanguageModel);

                    contentRetrieverWithExamples(graph, chatLanguageModel);

                    contentRetrieverWithoutRetries(graph, chatLanguageModel);
                }
            }
        }
    }

    private static void contentRetrieverWithMinimalConfig(Driver driver, Neo4jGraph graph, ChatModel chatLanguageModel) {
        // tag::retrieve-text2cypher[]
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
        // end::retrieve-text2cypher[]
    }

    private static void contentRetrieverWithExamples(Neo4jGraph graph, ChatModel chatLanguageModel) {
        // tag::retrieve-text2cypher-examples[]
        List<String> examples = List.of(
                """
                # Which streamer has the most followers?
                MATCH (s:Stream)
                RETURN s.name AS streamer
                ORDER BY s.followers DESC LIMIT 1
                """,
                """
                # How many streamers are from Norway?
                MATCH (s:Stream)-[:HAS_LANGUAGE]->(:Language {{name: 'Norwegian'}})
                RETURN count(s) AS streamers
                """);
        
        Neo4jText2CypherRetriever neo4jContentRetriever = Neo4jText2CypherRetriever.builder()
                .graph(graph)
                .chatModel(chatLanguageModel)
                // add the above examples
                .examples(examples)
                .build();
        
        final String textQuery = "Which streamer from Italy has the most followers?";
        Query query = new Query(textQuery);
        List<Content> contents = neo4jContentRetriever.retrieve(query);
        System.out.println(contents.get(0).textSegment().text());
        // output: "The most followed italian streamer"
        // end::retrieve-text2cypher-examples[]
    }

    private static void contentRetrieverWithoutRetries(Neo4jGraph graph, ChatModel chatLanguageModel) {
        Neo4jText2CypherRetriever retriever = Neo4jText2CypherRetriever.builder()
                .graph(graph)
                .chatModel(chatLanguageModel)
                .maxRetries(0) // disables retry logic
                .build();

        Query query = new Query("Who is the author of the book 'Dune'?");

        List<Content> contents = retriever.retrieve(query);

        System.out.println(contents.get(0).textSegment().text()); // "Frank Herbert"
    }

    private static void contentRetrieverWithSamplesAndMaxRels(ChatModel chatLanguageModel, Driver driver) {
        // Sample up to 3 example paths from the graph schema
        // Explore a maximum of 8 relationships from the start node
        try (Neo4jGraph graph = Neo4jGraph.builder().driver(driver).sample(3L).maxRels(8L).build()) {
            // tag::retrieve-text2cypher-sample-max-rels[]
            Neo4jText2CypherRetriever retriever = Neo4jText2CypherRetriever.builder()
                    .graph(graph)
                    .chatModel(chatLanguageModel)
                    .build();
            
            Query query = new Query("Who is the author of the book 'Dune'?");

            List<Content> contents = retriever.retrieve(query);

            System.out.println(contents.get(0).textSegment().text()); // "Frank Herbert"
            // end::retrieve-text2cypher-sample-max-rels[]
        }
    }
}
