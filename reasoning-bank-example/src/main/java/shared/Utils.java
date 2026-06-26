package shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

import static dev.langchain4j.internal.Utils.getOrDefault;

public class Utils {

    public static final String OPENAI_API_KEY = getOrDefault(System.getenv("OPENAI_API_KEY"), "demo");

    public static void startConversationWith(Assistant assistant) {
        Logger log = LoggerFactory.getLogger(Assistant.class);
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                log.info("==================================================");
                log.info("User: ");
                String userQuery = scanner.nextLine();
                log.info("==================================================");

                if ("exit".equalsIgnoreCase(userQuery)) {
                    break;
                }

                String agentAnswer = assistant.answer(userQuery);
                log.info("==================================================");
                log.info("Assistant: " + agentAnswer);
            }
        }
    }
}
