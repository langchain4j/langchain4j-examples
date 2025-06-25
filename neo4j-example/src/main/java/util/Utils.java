package util;

import dev.langchain4j.service.UserMessage;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    public interface Assistant {
        String chat(@UserMessage String userMessage);
    }

    public static Path toPath(String fileName) {
        try {
            return Paths.get(Utils.class.getClassLoader().getResource(fileName).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}