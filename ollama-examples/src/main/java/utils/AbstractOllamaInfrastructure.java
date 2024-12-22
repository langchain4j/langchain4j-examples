package utils;

import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static utils.OllamaImage.LLAMA_3_1;
import static utils.OllamaImage.localOllamaImage;

public class AbstractOllamaInfrastructure {

    public static final String OLLAMA_BASE_URL = System.getenv("OLLAMA_BASE_URL");
    public static final String MODEL_NAME = LLAMA_3_1;

    public static LangChain4jOllamaContainer ollama;

    static {
        if (isNullOrEmpty(OLLAMA_BASE_URL)) {
            String localOllamaImage = localOllamaImage(MODEL_NAME);
            ollama = new LangChain4jOllamaContainer(OllamaImage.resolve(OllamaImage.OLLAMA_IMAGE, localOllamaImage))
                    .withModel(MODEL_NAME);
            ollama.start();
            ollama.commitToImage(localOllamaImage);
        }
    }

    public static String ollamaBaseUrl(LangChain4jOllamaContainer ollama) {
        if (isNullOrEmpty(OLLAMA_BASE_URL)) {
            return ollama.getEndpoint();
        } else {
            return OLLAMA_BASE_URL;
        }
    }
}
