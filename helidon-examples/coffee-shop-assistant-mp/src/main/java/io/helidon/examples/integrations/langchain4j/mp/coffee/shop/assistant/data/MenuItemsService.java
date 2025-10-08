package io.helidon.examples.integrations.langchain4j.mp.coffee.shop.assistant.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * A service for managing and retrieving menu items from a JSON file.
 *
 * This service loads menu items from a specified JSON file path, which is provided
 * via the application configuration.
 */
@ApplicationScoped
public class MenuItemsService {
    private final Path jsonPath;

    /**
     * Constructs a {@code MenuItemsService} instance.
     *
     * @param jsonPath the file path to the menu items JSON file, injected from configuration
     */
    @Inject
    MenuItemsService(@ConfigProperty(name = "app.menu-items") String jsonPath) {
        this.jsonPath = Path.of(jsonPath);
    }

    /**
     * Retrieves the list of menu items from the JSON file.
     *
     * @return a list of {@link MenuItem} objects
     * @throws RuntimeException if an error occurs while reading or parsing the file
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
