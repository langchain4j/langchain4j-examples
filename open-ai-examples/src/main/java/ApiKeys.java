import static dev.langchain4j.internal.Utils.getOrDefault;

public class ApiKeys {

    public static final String OPENAI_API_KEY = getOrDefault(System.getenv("OPENAI_API_KEY"), "demo");
}
