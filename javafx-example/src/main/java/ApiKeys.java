import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApiKeys {

    private static final Logger LOGGER = LogManager.getLogger(ApiKeys.class);
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ApiKeys.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                String message = "Could not find 'application.properties' in the classpath (resources folder).";
                LOGGER.error(message);
                throw new RuntimeException(message);
            }

            properties.load(input);
            LOGGER.info("'application.properties' loaded successfully.");

        } catch (IOException e) {
            String message = "Failed to load 'application.properties' file.";
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String getOpenAiApiKey() {
        String apiKey = get("openai.api.key");

        if (apiKey == null || apiKey.isBlank()) {
            String message = "'openai.api.key' is missing or empty in 'application.properties'. Please define it to proceed.";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }

        return apiKey;
    }
}