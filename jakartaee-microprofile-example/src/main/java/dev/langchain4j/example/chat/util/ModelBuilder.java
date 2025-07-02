package dev.langchain4j.example.chat.util;

import static dev.langchain4j.model.github.GitHubModelsChatModelName.PHI_3_MINI_INSTRUCT_4K;
import static dev.langchain4j.model.github.GitHubModelsEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;
import static dev.langchain4j.model.huggingface.HuggingFaceModelName.SENTENCE_TRANSFORMERS_ALL_MINI_LM_L6_V2;
import static dev.langchain4j.model.mistralai.MistralAiChatModelName.MISTRAL_SMALL_LATEST;
import static dev.langchain4j.model.mistralai.MistralAiEmbeddingModelName.MISTRAL_EMBED;
import static java.time.Duration.ofSeconds;

import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import dev.langchain4j.model.github.GitHubModelsEmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceLanguageModel;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaLanguageModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ModelBuilder {

    private static Logger logger = Logger.getLogger(ModelBuilder.class.getName());

    @Inject
    @ConfigProperty(name = "hugging.face.api.key")
    private String HUGGING_FACE_API_KEY;

    @Inject
    @ConfigProperty(name = "hugging.face.language.model.id")
    private String HUGGING_FACE_LANGUAGE_MODEL_ID;

    @Inject
    @ConfigProperty(name = "hugging.face.chat.model.id")
    private String HUGGING_FACE_CHAT_MODEL_ID;

    @Inject
    @ConfigProperty(name = "github.api.key")
    private String GITHUB_API_KEY;

    @Inject
    @ConfigProperty(name = "github.chat.model.id")
    private String GITHUB_CHAT_MODEL_ID;

    @Inject
    @ConfigProperty(name = "ollama.base.url")
    private String OLLAMA_BASE_URL;

    @Inject
    @ConfigProperty(name = "ollama.chat.model.id")
    private String OLLAMA_CHAT_MODEL_ID;

    @Inject
    @ConfigProperty(name = "mistral.ai.api.key")
    private String MISTRAL_AI_API_KEY;

    @Inject
    @ConfigProperty(name = "mistral.ai.chat.model.id")
    private String MISTRAL_AI_MISTRAL_CHAT_MODEL_ID;

    @Inject
    @ConfigProperty(name = "chat.model.timeout")
    private Integer TIMEOUT;

    @Inject
    @ConfigProperty(name = "chat.model.max.token")
    private Integer MAX_NEW_TOKEN;

    @Inject
    @ConfigProperty(name = "chat.model.temperature")
    private Double TEMPERATURE;

    private LanguageModel languageModel = null;
    private EmbeddingModel embeddingModel = null;
    private ChatModel chatModelForResource = null;
    private ChatModel chatModelForWeb = null;

    public boolean usingGithub() {
        return GITHUB_API_KEY.startsWith("ghp_");
    }

    public boolean usingOllama() {
        return OLLAMA_BASE_URL.startsWith("http");
    }

    public boolean usingMistralAi() {
        return MISTRAL_AI_API_KEY.length() > 30;
    }

    public boolean usingHuggingFace() {
        return HUGGING_FACE_API_KEY.startsWith("hf_");
    }

    public LanguageModel getLanguageModel() throws Exception {
        if (languageModel == null) {
            if (usingGithub()) {
                throw new Exception("LangChain4J Github APIs do not support language model");
            } else if (usingOllama()) {
                languageModel = OllamaLanguageModel.builder()
                    .baseUrl(OLLAMA_BASE_URL)
                    .modelName("tinydolphin")
                    .temperature(1.0)
                    .build();
                logger.info("using Ollama tinydolphin language model");
            } else if (usingMistralAi()) {
                throw new Exception("LangChain4J  Mistral AI APIs do not support language model");
            } else if (usingHuggingFace()) {
                languageModel = HuggingFaceLanguageModel.builder()
                    .accessToken(HUGGING_FACE_API_KEY)
                    .modelId(HUGGING_FACE_LANGUAGE_MODEL_ID)
                    .timeout(ofSeconds(120))
                    .temperature(1.0)
                    .maxNewTokens(30)
                    .waitForModel(true)
                    .build();
                logger.info("using Hugging Face " + HUGGING_FACE_LANGUAGE_MODEL_ID + " language model");
            } else {
                throw new Exception("No available platform to access model");
            }
        }
        return languageModel;
    }

    public EmbeddingModel getEmbeddingModel() throws Exception {
        if (embeddingModel == null) {
            if (usingGithub()) {
                embeddingModel = GitHubModelsEmbeddingModel.builder()
                    .gitHubToken(GITHUB_API_KEY)
                    .modelName(TEXT_EMBEDDING_3_SMALL)
                    .timeout(ofSeconds(120))
                    .build();
                logger.info("using Github " + TEXT_EMBEDDING_3_SMALL + " embedding model");
            } else if (usingOllama()) {
                embeddingModel = OllamaEmbeddingModel.builder()
                    .baseUrl(OLLAMA_BASE_URL)
                    .modelName("all-minilm")
                    .timeout(ofSeconds(120))
                    .build();
                logger.info("using Ollama all-minilm embedding model");
            } else if (usingMistralAi()) {
                embeddingModel = MistralAiEmbeddingModel.builder()
                    .apiKey(MISTRAL_AI_API_KEY)
                    .modelName(MISTRAL_EMBED)
                    .timeout(ofSeconds(120))
                    .build();
                logger.info("using Mistral AI " + MISTRAL_EMBED + " embedding model");
            } else if (usingHuggingFace()) {
                embeddingModel = HuggingFaceEmbeddingModel.builder()
                    .accessToken(HUGGING_FACE_API_KEY)
                    .modelId(SENTENCE_TRANSFORMERS_ALL_MINI_LM_L6_V2)
                    .timeout(ofSeconds(120))
                    .waitForModel(true)
                    .build();
                logger.info("using Hugging Face " + SENTENCE_TRANSFORMERS_ALL_MINI_LM_L6_V2 + " embedding model");
            } else {
                throw new Exception("No available platform to access model");
            }
        }
        return embeddingModel;
    }

    public ChatModel getChatModelForResource() throws Exception {
        if (chatModelForResource == null) {
            if (usingGithub()) {
                chatModelForResource = GitHubModelsChatModel.builder()
                    .gitHubToken(GITHUB_API_KEY)
                    .modelName(PHI_3_MINI_INSTRUCT_4K)
                    .timeout(ofSeconds(120))
                    .temperature(1.0)
                    .maxTokens(200)
                    .build();
                logger.info("using Github " + PHI_3_MINI_INSTRUCT_4K + " chat model");
            } else if (usingOllama()) {
                chatModelForResource = OllamaChatModel.builder()
                    .baseUrl(OLLAMA_BASE_URL)
                    .modelName(OLLAMA_CHAT_MODEL_ID)
                    .timeout(ofSeconds(TIMEOUT))
                    .temperature(TEMPERATURE)
                    .build();
                logger.info("using Ollama " + OLLAMA_CHAT_MODEL_ID + " chat model");
            } else if (usingMistralAi()) {
                chatModelForResource = MistralAiChatModel.builder()
                    .apiKey(MISTRAL_AI_API_KEY)
                    .modelName(MISTRAL_SMALL_LATEST)
                    .timeout(ofSeconds(TIMEOUT))
                    .temperature(TEMPERATURE)
                    .maxTokens(MAX_NEW_TOKEN)
                    .build();
                logger.info("using Mistral AI " + MISTRAL_SMALL_LATEST + " chat model");
            } else if (usingHuggingFace()) {
                chatModelForResource = HuggingFaceChatModel.builder()
                    .accessToken(HUGGING_FACE_API_KEY)
                    .modelId(HUGGING_FACE_LANGUAGE_MODEL_ID)
                    .timeout(ofSeconds(120))
                    .temperature(1.0)
                    .maxNewTokens(200)
                    .waitForModel(true)
                    .build();
                logger.info("using Hugging Face " + HUGGING_FACE_LANGUAGE_MODEL_ID + " chat model");
            } else {
                throw new Exception("No available platform to access model");
            }
        }
        return chatModelForResource;
    }

    public ChatModel getChatModelForWeb() throws Exception {
        if (chatModelForWeb == null) {
            if (usingGithub()) {
                chatModelForWeb = GitHubModelsChatModel.builder()
                    .gitHubToken(GITHUB_API_KEY)
                    .modelName(GITHUB_CHAT_MODEL_ID)
                    .timeout(ofSeconds(TIMEOUT))
                    .temperature(TEMPERATURE)
                    .maxTokens(MAX_NEW_TOKEN)
                    .build();
                logger.info("using Github " + GITHUB_CHAT_MODEL_ID + " chat model for the web");
            } else if (usingOllama()) {
                chatModelForWeb = OllamaChatModel.builder()
                    .baseUrl(OLLAMA_BASE_URL)
                    .modelName(OLLAMA_CHAT_MODEL_ID)
                    .timeout(ofSeconds(TIMEOUT))
                    .temperature(TEMPERATURE)
                    .build();
                logger.info("using Ollama " + OLLAMA_CHAT_MODEL_ID + " chat model for the web");
            } else if (usingMistralAi()) {
                chatModelForWeb = MistralAiChatModel.builder()
                    .apiKey(MISTRAL_AI_API_KEY)
                    .modelName(MISTRAL_AI_MISTRAL_CHAT_MODEL_ID)
                    .timeout(ofSeconds(TIMEOUT))
                    .temperature(TEMPERATURE)
                    .maxTokens(MAX_NEW_TOKEN)
                    .build();
                logger.info("using Mistral AI " + MISTRAL_AI_MISTRAL_CHAT_MODEL_ID + " chat model for the web");
            } else if (usingHuggingFace()) {
                chatModelForWeb = HuggingFaceChatModel.builder()
                    .accessToken(HUGGING_FACE_API_KEY)
                    .modelId(HUGGING_FACE_CHAT_MODEL_ID)
                    .timeout(ofSeconds(TIMEOUT))
                    .temperature(TEMPERATURE)
                    .maxNewTokens(MAX_NEW_TOKEN)
                    .waitForModel(true)
                    .build();
                logger.info("using Hugging Face " + HUGGING_FACE_CHAT_MODEL_ID + " chat model for the web");

            } else {
                throw new Exception("No available platform to access model");
            }
        }
        return chatModelForWeb;
    }

}
