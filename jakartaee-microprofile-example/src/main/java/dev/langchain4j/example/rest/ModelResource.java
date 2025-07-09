package dev.langchain4j.example.rest;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceLanguageModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.Properties;

import static dev.langchain4j.data.segment.TextSegment.textSegment;
import static dev.langchain4j.model.huggingface.HuggingFaceModelName.SENTENCE_TRANSFORMERS_ALL_MINI_LM_L6_V2;
import static dev.langchain4j.store.embedding.CosineSimilarity.between;
import static dev.langchain4j.store.embedding.RelevanceScore.fromCosineSimilarity;
import static java.time.Duration.ofSeconds;

@ApplicationScoped
@Path("model")
public class ModelResource {

    @Inject
    @ConfigProperty(name = "hugging.face.api.key")
    private String HUGGING_FACE_API_KEY;

    @Inject
    @ConfigProperty(name = "language.model.id")
    private String LANGUAGE_MODEL_ID;

    private HuggingFaceLanguageModel languageModel = null;
    private HuggingFaceEmbeddingModel embeddingModel = null;

    private HuggingFaceLanguageModel getLanguageModel() {
        if (languageModel == null) {
            languageModel = HuggingFaceLanguageModel.builder()
                    .accessToken(HUGGING_FACE_API_KEY)
                    .modelId(LANGUAGE_MODEL_ID)
                    .timeout(ofSeconds(120))
                    .temperature(1.0)
                    .maxNewTokens(30)
                    .waitForModel(true)
                    .build();
        }
        return languageModel;
    }

    private HuggingFaceEmbeddingModel getEmbeddingModel() {
        if (embeddingModel == null) {
            embeddingModel = HuggingFaceEmbeddingModel.builder()
                    .accessToken(HUGGING_FACE_API_KEY)
                    .modelId(SENTENCE_TRANSFORMERS_ALL_MINI_LM_L6_V2)
                    .timeout(ofSeconds(120))
                    .waitForModel(true)
                    .build();
        }
        return embeddingModel;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("language")
    @Operation(
            summary = "Use the language model.",
            description = "Provide a sequence of words to a large language model.",
            operationId = "languageModelAsk")
    public String languageModelAsk(@QueryParam("question") String question) {

        HuggingFaceLanguageModel model = getLanguageModel();

        String answer;
        try {
            answer = model.generate(question).content();
        } catch (Exception e) {
            answer = "My failure reason is:\n\n" + e.getMessage();
        }

        return answer;

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("chat")
    @Operation(
            summary = "Use the chat model.",
            description = "Assume you are talking with an agent that is knowledgeable about " +
                    "Large Language Models. Ask any question about it.",
            operationId = "chatModelAsk")
    public List<String> chatModelAsk(@QueryParam("userMessage") String userMessage) {

        HuggingFaceChatModel model = HuggingFaceChatModel.builder()
                .accessToken(HUGGING_FACE_API_KEY)
                .modelId(LANGUAGE_MODEL_ID)
                .timeout(ofSeconds(120))
                .temperature(1.0)
                .maxNewTokens(200)
                .waitForModel(true)
                .build();

        SystemMessage systemMessage = SystemMessage.from(
                "You are very knowledgeable about Large Language Models. Be friendly. Give concise answers.");

        AiMessage aiMessage = model.chat(systemMessage, UserMessage.from(userMessage)).aiMessage();

        return List.of(
                "System: " + systemMessage.text(),
                "Me:     " + userMessage,
                "Agent:  " + aiMessage.text().trim());

    }

    private Properties getProperties(String value, Embedding embedding) {
        Properties p = new Properties();
        p.put("words", value.split(" "));
        p.put("embedding-vector", embedding.vectorAsList());
        return p;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("similarity")
    @Operation(
            summary = "Use the embedding model.",
            description = "Determine the similarity and relevance score of two sentences.",
            operationId = "similarity")
    public Properties similarity(
            @QueryParam("text1") String text1,
            @QueryParam("text2") String text2) {

        HuggingFaceEmbeddingModel model = getEmbeddingModel();

        List<TextSegment> textSegments = List.of(textSegment(text1), textSegment(text2));
        List<Embedding> embeddings = model.embedAll(textSegments).content();
        double similarity = between(embeddings.get(0), embeddings.get(1));

        Properties p = new Properties();
        p.put("text1", getProperties(text1, embeddings.get(0)));
        p.put("text2", getProperties(text2, embeddings.get(1)));
        p.put("similarity", similarity);
        p.put("relevance-score", fromCosineSimilarity(similarity));

        return p;

    }
}
