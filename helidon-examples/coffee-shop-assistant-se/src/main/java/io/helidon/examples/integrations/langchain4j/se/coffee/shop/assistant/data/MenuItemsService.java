package io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import io.helidon.common.config.Config;
import io.helidon.service.registry.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A service for managing and retrieving menu items from a JSON file.
 */
@Service.Singleton
public class MenuItemsService {
    private static final String CONFIG_KEY = "app.menu-items";
    private final Path jsonPath;

    /**
     * Constructs a {@code MenuItemsService} instance.
     *
     * @param config the configuration containing the path to the menu items JSON file
     * @throws IllegalStateException if the configuration key {@code app.menu-items} is missing
     */
    @Service.Inject
    MenuItemsService(Config config) {
        this.jsonPath = config.get(CONFIG_KEY)
                .as(Path.class)
                .orElseThrow(() -> new IllegalStateException(CONFIG_KEY + " is a required configuration key for RAG"));
    }

    /**
     * Retrieves the list of menu items from the configured JSON file.
     *
     * @return a list of {@link MenuItem} objects
     * @throws RuntimeException if an error occurs while reading the file
     */
    public List<MenuItem> getMenuItems() {
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonPath.toFile(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to read menu items from file: " + jsonPath, e);
        }
    }
}
