import dev.langchain4j.community.data.document.graph.GraphDocument;
import dev.langchain4j.community.data.document.transformer.graph.LLMGraphTransformer;
import dev.langchain4j.community.rag.content.retriever.neo4j.KnowledgeGraphWriter;
import dev.langchain4j.community.rag.content.retriever.neo4j.Neo4jGraph;
import dev.langchain4j.data.document.DefaultDocument;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.testcontainers.containers.Neo4jContainer;

import java.util.List;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class KnowledgeGraphWriterExample {
    private static final String EXAMPLES_PROMPT =
            """
                    [
                       {
                          "tail":"Microsoft",
                          "head":"Adam",
                          "head_type":"Person",
                          "text":"Adam is a software engineer in Microsoft since 2009, and last year he got an award as the Best Talent",
                          "relation":"WORKS_FOR",
                          "tail_type":"Company"
                       },
                       {
                          "tail":"Best Talent",
                          "head":"Adam",
                          "head_type":"Person",
                          "text":"Adam is a software engineer in Microsoft since 2009, and last year he got an award as the Best Talent",
                          "relation":"HAS_AWARD",
                          "tail_type":"Award"
                       },
                       {
                          "tail":"Microsoft",
                          "head":"Microsoft Word",
                          "head_type":"Product",
                          "text":"Microsoft is a tech company that provide several products such as Microsoft Word",
                          "relation":"PRODUCED_BY",
                          "tail_type":"Company"
                       },
                       {
                          "tail":"lightweight app",
                          "head":"Microsoft Word",
                          "head_type":"Product",
                          "text":"Microsoft Word is a lightweight app that accessible offline",
                          "relation":"HAS_CHARACTERISTIC",
                          "tail_type":"Characteristic"
                       },
                       {
                          "tail":"accessible offline",
                          "head":"Microsoft Word",
                          "head_type":"Product",
                          "text":"Microsoft Word is a lightweight app that accessible offline",
                          "relation":"HAS_CHARACTERISTIC",
                          "tail_type":"Characteristic"
                       }
                    ]
                    """;

    public static String CAT_ON_THE_TABLE = "Sylvester the cat is on the table";
    public static String KEANU_REEVES_ACTED = "Keanu Reeves acted in Matrix";
    public static final String OPENAI_API_KEY = getOrDefault(System.getenv("OPENAI_API_KEY"), "demo");
    public static final String OPENAI_BASE_URL = "demo".equals(OPENAI_API_KEY) ? "http://langchain4j.dev/demo/openai/v1" : null;
    
    public static void main(String[] args) {
        final OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY)
                .baseUrl(OPENAI_BASE_URL)
                .modelName(GPT_4_O_MINI)
                .build();
        
        LLMGraphTransformer graphTransformer = LLMGraphTransformer.builder()
                .model(model)
                .examples(EXAMPLES_PROMPT)
                .build();

        Document docKeanu = new DefaultDocument(KEANU_REEVES_ACTED);
        Document docCat = new DefaultDocument(CAT_ON_THE_TABLE);
        List<Document> documents = List.of(docCat, docKeanu);

        List<GraphDocument> graphDocuments = graphTransformer.transformAll(documents);

        try (Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:5.26")
                .withAdminPassword("admin1234")
                .withLabsPlugins("apoc")) {
            neo4jContainer.start();
            Neo4jGraph graph = Neo4jGraph.builder()
                    .withBasicAuth(neo4jContainer.getBoltUrl(), "neo4j", neo4jContainer.getAdminPassword())
                    .build();

            KnowledgeGraphWriter writer = KnowledgeGraphWriter.builder()
                    .graph(graph)
                    .label("Entity")
                    .relType("MENTIONS")
                    .idProperty("id")
                    .textProperty("text")
                    .build();

            // `graphDocuments` obtained from LLMGraphTransformer
            writer.addGraphDocuments(graphDocuments, true); // set to true to include document source

            /*
            The above KnowledgeGraphWriter will add paths like:
            (:Document {id: UUID, text: 'Sylvester the cat is on the table'})-[:MENTIONS]->(:Entity:Animal {id: 'Sylvester the cat'})-[:IS_ON]->(:Entity:Object {id: 'table'})
            (Document {id: UUID, text: 'Keanu Reeves acted in Matrix'})-[:MENTIONS]->(:Entity:Person {id: 'Keanu Reeves'})-[:ACTED_IN]->(:Entity:Movie  {id: 'Matrix'})
             */

            KnowledgeGraphWriter writerWithoutDocs = KnowledgeGraphWriter.builder()
                    .graph(graph)
                    .label("FooBar")
                    .relType("MENTIONS")
                    .idProperty("id")
                    .textProperty("text")
                    .build();

            // `graphDocuments` obtained from LLMGraphTransformer
            writerWithoutDocs.addGraphDocuments(graphDocuments, false); // set to true not to include document source
            /*
            The above KnowledgeGraphWriter will add paths like:
            (:FooBar:Animal {id: 'Sylvester the cat'})-[:IS_ON]->(:FooBar:Object {id: 'table'})
            (:FooBar:Person {id: 'Keanu Reeves'})-[:ACTED_IN]->(:FooBar:Movie  {id: 'Matrix'})
             */

            graph.close();
        }
    }
}
