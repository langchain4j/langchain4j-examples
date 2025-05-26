package dev.langchain4j.example.chat.util;

import static dev.langchain4j.model.github.GitHubModelsChatModelName.GPT_4_O_MINI;
import static dev.langchain4j.model.github.GitHubModelsChatModelName.PHI_3_MINI_INSTRUCT_4K;
import static dev.langchain4j.model.github.GitHubModelsEmbeddingModelName.TEXT_EMBEDDING_3_SMALL;
import static dev.langchain4j.model.huggingface.HuggingFaceModelName.SENTENCE_TRANSFORMERS_ALL_MINI_LM_L6_V2;
import static java.time.Duration.ofSeconds;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import dev.langchain4j.model.github.GitHubModelsEmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceLanguageModel;
import dev.langchain4j.model.language.LanguageModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ModelBuilder {

    @Inject
    @ConfigProperty(name = "hugging.face.api.key")
    private String HUGGING_FACE_API_KEY;

    @Inject
    @ConfigProperty(name = "github.api.key")
    private String GITHUB_API_KEY;

    @Inject
    @ConfigProperty(name = "hugging.language.model.id")
    private String HUGGING_FACE_LANGUAGE_MODEL_ID;

    @Inject
    @ConfigProperty(name = "hugging.chat.model.id")
    private String HUGGING_FACE_CHAT_MODEL_ID;

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

    public boolean usingHuggingFace() {
    	return HUGGING_FACE_API_KEY.startsWith("hf_");
    }

    public boolean usingGithub() {
    	return GITHUB_API_KEY.startsWith("ghp_");
    }

    public LanguageModel getLanguageModel() throws Exception {
        if (languageModel == null) {
        	if (usingHuggingFace()) {
                languageModel = HuggingFaceLanguageModel.builder()
                    .accessToken(HUGGING_FACE_API_KEY)
                    .modelId(HUGGING_FACE_LANGUAGE_MODEL_ID)
                    .timeout(ofSeconds(120))
                    .temperature(1.0)
                    .maxNewTokens(30)
                    .waitForModel(true)
                    .build();
        	} else if (usingGithub()) {
           		throw new Exception("LangChain4J Github APIs do not support language model");
        	} else {
        		throw new Exception("No available platform to access model");
            }
        }
        return languageModel;
    }

    public EmbeddingModel getEmbeddingModel() throws Exception {
        if (embeddingModel == null) {
        	if (usingHuggingFace()) {
        		embeddingModel = HuggingFaceEmbeddingModel.builder()
                    .accessToken(HUGGING_FACE_API_KEY)
                    .modelId(SENTENCE_TRANSFORMERS_ALL_MINI_LM_L6_V2)
                    .timeout(ofSeconds(120))
                    .waitForModel(true)
                    .build();
        	} else if (usingGithub()) {
        		embeddingModel = GitHubModelsEmbeddingModel.builder()
                        .gitHubToken(GITHUB_API_KEY)
                        .modelName(TEXT_EMBEDDING_3_SMALL)
                        .timeout(ofSeconds(120))
                        .build();
        	} else {
        		throw new Exception("No available platform to access model");
            }
        }
        return embeddingModel;
    }

    public ChatModel getChatModelForResource() throws Exception {
        if (chatModelForResource == null) {
        	if (usingHuggingFace()) {
        	    chatModelForResource = HuggingFaceChatModel.builder()
                    .accessToken(HUGGING_FACE_API_KEY)
                    .modelId(HUGGING_FACE_LANGUAGE_MODEL_ID)
                    .timeout(ofSeconds(120))
                    .temperature(1.0)
                    .maxNewTokens(200)
                    .waitForModel(true)
                    .build();
        	} else if (usingGithub()) {
        	    chatModelForResource = GitHubModelsChatModel.builder()
            		    .gitHubToken(GITHUB_API_KEY)
            		    .modelName(PHI_3_MINI_INSTRUCT_4K)
                        .timeout(ofSeconds(120))
                        .temperature(1.0)
                        .maxTokens(200)
                        .build();
        	} else {
        		throw new Exception("No available platform to access model");
        	}
        }
        return chatModelForResource;
    }

    public ChatModel getChatModelForWeb() throws Exception {
        if (chatModelForWeb == null) {
        	if (usingHuggingFace()) {
        	    chatModelForWeb = HuggingFaceChatModel.builder()
                    .accessToken(HUGGING_FACE_API_KEY)
                    .modelId(HUGGING_FACE_CHAT_MODEL_ID)
                    .timeout(ofSeconds(TIMEOUT))
                    .temperature(TEMPERATURE)
                    .maxNewTokens(MAX_NEW_TOKEN)
                    .waitForModel(true)
                    .build();
        	} else if (usingGithub()) {
        		chatModelForWeb = GitHubModelsChatModel.builder()
            		    .gitHubToken(GITHUB_API_KEY)
            		    .modelName(GPT_4_O_MINI)
            		    .timeout(ofSeconds(TIMEOUT))
                        .temperature(TEMPERATURE)
                        .maxTokens(MAX_NEW_TOKEN)
            		    .build();
        	} else {
        		throw new Exception("No available platform to access model");
        	}
        }
        return chatModelForWeb;
    }

}
