package util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for loading string content from resource files.
 * Handles proper resource management and closing of input streams.
 */
public class StringLoader {
    
    /**
     * Loads a string from a resource file using the class loader.
     * The resource path should start with "/" and be relative to the classpath root.
     * 
     * @param resourcePath the path to the resource file (e.g., "/documents/user_life_story.txt")
     * @return the content of the resource file as a string
     * @throws IOException if the resource cannot be read
     */
    public static String loadFromResource(String resourcePath) throws IOException {
        try (InputStream inputStream = StringLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes());
        }
    }
    
    /**
     * Loads a string from a resource file using a specific class's class loader.
     * This is useful when you want to load resources relative to a specific class.
     * 
     * @param clazz the class to use for loading the resource
     * @param resourcePath the path to the resource file (e.g., "/documents/user_life_story.txt")
     * @return the content of the resource file as a string
     * @throws IOException if the resource cannot be read
     */
    public static String loadFromResource(Class<?> clazz, String resourcePath) throws IOException {
        try (InputStream inputStream = clazz.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath + " for class: " + clazz.getName());
            }
            return new String(inputStream.readAllBytes());
        }
    }
}
