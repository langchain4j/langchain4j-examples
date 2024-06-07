public class OllamaContants {

    static final String MODEL_ORCA_MINI = "orca-mini";
    static final String MODEL_MISTRAL = "mistral";
    static final String MODEL_LLAMA2 = "llama2";
    static final String MODEL_CODE_LLAMA = "codellama";
    static final String MODEL_PHI = "phi";
    static final String MODEL_TINY_LLAMA = "tinyllama";

    // try "mistral", "llama2", "codellama", "phi" or "tinyllama"
    static final String MODEL_NAME = MODEL_ORCA_MINI;
    public static final String OLLAMA_IMAGE_NAME = "langchain4j/ollama-" + MODEL_NAME + ":latest";
    public static final Integer OLLAMA_PORT = 11434;
}
