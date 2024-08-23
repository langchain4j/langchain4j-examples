import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The DbPediaSparqlExample class is a singleton that provides methods to execute SPARQL queries
 * against the DBpedia SPARQL endpoint. It also integrates with the Azure OpenAI model to
 * extract subjects and generate answers based on text data. The class includes methods for creating
 * and executing SPARQL queries, and processing the results.
 */
public class DbPediaSparqlExample {

    // Logger to log messages for debugging and information purposes
    private static final Logger logger = Logger.getLogger(DbPediaSparqlExample.class.getName());

    // Singleton instance of the class, initialized during class loading
    private static final DbPediaSparqlExample instance = new DbPediaSparqlExample();

    // Private constructor to prevent external instantiation
    private DbPediaSparqlExample() {
        // Initialize logger or other configurations if necessary
    }

    // Static method to access the singleton instance
    public static DbPediaSparqlExample getInstance() {
        return instance;
    }

    /**
     * Constructs a SPARQL query string to retrieve the abstract of a given subject from DBpedia.
     *
     * @param subject The subject (e.g., a person or entity) to query in DBpedia.
     * @return A SPARQL query string.
     */
    private String extractAbstractSparqlQuery(String subject) {
        // Using StringBuilder to build the SPARQL query string
        StringBuilder queryString = new StringBuilder()
            .append("PREFIX dbo: <http://dbpedia.org/ontology/> \n")
            .append("PREFIX dbr: <http://dbpedia.org/resource/> \n")
            .append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n")
            .append("\n")
            .append("SELECT ?abstract\n")
            .append("WHERE {\n")
            .append("  dbr:").append(subject).append(" dbo:abstract ?abstract .\n")
            .append("  FILTER (lang(?abstract) = \"en\")\n")
            .append("}\n");
        return queryString.toString();
    }

    /**
     * Executes a SPARQL query against the DBpedia SPARQL endpoint.
     *
     * @param queryString The SPARQL query to be executed.
     * @return The results of the query as a string, or a message if no results are found.
     */
    public String executeSparqlQuery(String queryString) {
        // Creating a SPARQL query object
        Query query = QueryFactory.create(queryString);
        // Using try-with-resources to ensure that QueryExecution is closed after use
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query)) {
            // Executing the query and getting the results
            ResultSet results = qexec.execSelect();
            // Checking if there are any results
            if (results.hasNext()) {
                // Formatting the results into a readable string format
                return ResultSetFormatter.asText(results);
            } else {
                logger.info("No results found.");
            }
        } catch (Exception e) {
            // Logging any errors that occur during query execution
            logger.log(Level.SEVERE, "Error executing query: " + e.getMessage(), e);
        }
        // Returning a message if no results are found
        return "No results found.";
    }

    /**
     * The main method is the entry point of the application.
     * It performs the following tasks:
     * 1. Retrieves the singleton instance of DbPediaSparqlExample and AzureOpenAIConfig.
     * 2. Validates the Azure OpenAI configuration.
     * 3. Creates and configures an AzureOpenAiChatModel instance.
     * 4. Extracts the subject from a given question using the Azure OpenAI model.
     * 5. Executes a SPARQL query to retrieve the abstract of the extracted subject from DBpedia.
     * 6. Generates an answer to the question using the subject, abstract, and Azure OpenAI model.
     * 7. Logs the identified subject, retrieved abstract, and generated answer.
     *
     * @param args Command line arguments (not used in this implementation).
     * @throws IOException If an I/O error occurs.
     */
    public static void main(String[] args) throws IOException {
        // Getting the singleton instance of DbPediaSparqlExample
        DbPediaSparqlExample dbpedia = DbPediaSparqlExample.getInstance();
        // Getting the singleton instance of AzureOpenAIConfig
        AzureOpenAIConfig config = AzureOpenAIConfig.getInstance();

        // Check configuration before proceeding
        if (config == null || config.getApiKey() == null || config.getEndpoint() == null || config.getDeploymentName() == null) {
            logger.severe("Azure OpenAI configuration is incomplete.");
            return;
        }

        // Creating an instance of AzureOpenAiChatModel with the necessary configurations
        AzureOpenAiChatModel model = AzureOpenAiChatModel.builder()
            .apiKey(config.getApiKey())
            .endpoint(config.getEndpoint())
            .deploymentName(config.getDeploymentName())
            .temperature(0.3)
            .logRequestsAndResponses(true)
            .build();

        // The question to be processed by the model
        String question = "How many years did Napoleon live?";

        // Extracting the subject of the question using the Azure OpenAI model
        String theSubject = extractSubject(model, question);
        logger.info("Identified subject: " + theSubject);

        // Retrieving the abstract for the extracted subject from DBpedia
        String theAbstract = dbpedia.executeSparqlQuery(dbpedia.extractAbstractSparqlQuery(theSubject));
        logger.info("Abstract for the subject: " + theAbstract);

        // Generating an answer to the question based on the abstract and the subject
        String theAnswer = generateAnswer(model, theSubject, theAbstract, question);
        logger.info("Answer to the question: " + theAnswer);
        logger.info("Done!");
        System.exit(0);
    }

    /**
     * Extracts the subject from a given question using the Azure OpenAI model.
     *
     * @param model    The AzureOpenAiChatModel used for processing.
     * @param question The question from which the subject needs to be extracted.
     * @return The extracted subject as a string.
     */
    private static String extractSubject(AzureOpenAiChatModel model, String question) {
        // Creating a prompt template to instruct the model on how to extract the subject
        PromptTemplate extractSubjectPromptTemplate = PromptTemplate.from(
            "As an expert in the Semantic Web, RDF/RDFS and SPARQL syntaxes:\n"
            + "Question:\n"
            + "In a subject/predicate/object structure, extract the subject of this phrase {{question}}\n"
            + "Just return the subject, no comment, no extra information, ex.: a_subject"
        );
        // Creating a map to hold variables to be passed to the prompt template
        Map<String, Object> variables = new HashMap<>();
        variables.put("question", question);
        // Applying the variables to the prompt template
        Prompt prompt = extractSubjectPromptTemplate.apply(variables);
        // Generating and returning the subject using the Azure OpenAI model
        return model.generate(prompt.text());
    }

    /**
     * Generates an answer to a given question using the Azure OpenAI model, the subject, and abstract text.
     *
     * @param model        The AzureOpenAiChatModel used for processing.
     * @param subject      The subject identified from the question.
     * @param abstractText The abstract text retrieved from DBpedia for the subject.
     * @param question     The question that needs to be answered.
     * @return The generated answer as a string.
     */
    private static String generateAnswer(AzureOpenAiChatModel model, String subject, String abstractText, String question) {
        // Creating a prompt template to instruct the model on how to generate an answer
        PromptTemplate extractAnswerPromptTemplate = PromptTemplate.from(
            "As an expert in text understanding\n"
            + "Here is a text about \"{{aSubject}}\"\n"
            + "<text begin>\n"
            + "{{theText}}\n"
            + "<text end>\n"
            + "now answers the following question in a professional manner:\n"
            + "<text begin>\n"
            + "{{question}}\n"
            + "<text end>\n"
        );
        // Creating a map to hold variables to be passed to the prompt template
        Map<String, Object> variables = new HashMap<>();
        variables.put("aSubject", subject);
        variables.put("theText", abstractText);
        variables.put("question", question);
        // Applying the variables to the prompt template
        Prompt prompt = extractAnswerPromptTemplate.apply(variables);
        // Generating and returning the answer using the Azure OpenAI model
        return model.generate(prompt.text());
    }
}
