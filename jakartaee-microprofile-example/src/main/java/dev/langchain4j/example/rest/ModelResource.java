package dev.langchain4j.example.rest;

import static dev.langchain4j.data.segment.TextSegment.textSegment;
import static dev.langchain4j.store.embedding.CosineSimilarity.between;
import static dev.langchain4j.store.embedding.RelevanceScore.fromCosineSimilarity;

import java.util.List;
import java.util.Properties;

import org.eclipse.microprofile.openapi.annotations.Operation;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.example.chat.util.ModelBuilder;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.language.LanguageModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
@Path("model")
public class ModelResource {

    @Inject
    private ModelBuilder modelBuilder;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("language")
    @Operation(
            summary = "Use the language model.",
            description = "Provide a sequence of words to a large language model.",
            operationId = "languageModelAsk")
    public String languageModelAsk(@QueryParam("question") String question) throws Exception {

        LanguageModel model = modelBuilder.getLanguageModel();

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
    public List<String> chatModelAsk(@QueryParam("userMessage") String userMessage) throws Exception {

        ChatModel model = modelBuilder.getChatModelForResource();

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
            @QueryParam("text2") String text2) throws Exception {

        EmbeddingModel model = modelBuilder.getEmbeddingModel();

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
