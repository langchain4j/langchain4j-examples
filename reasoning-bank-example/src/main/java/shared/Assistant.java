package shared;

/**
 * This is an "AI Service". It is a Java service with AI capabilities/features.
 * It can be integrated into your code like any other service, acting as a bean, and can be mocked for testing.
 * The goal is to seamlessly integrate AI functionality into your (existing) codebase with minimal friction.
 * <br>
 * More info here: https://docs.langchain4j.dev/tutorials/ai-services
 */
public interface Assistant {

    String answer(String query);
}
