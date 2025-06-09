package dev.langchain4j.example.resource;

import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("openai")
public class OpenAiChatModelResource {

    @Inject
    @ConfigProperty(name = "openai.api.key")
    private String openAiApiKey;

    @Inject
    @ConfigProperty(name = "openai.chat.model")
    private String modelName;

    private OpenAiChatModel chatModel;

    @PostConstruct
    public void init() {
        chatModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(modelName)
                .build();
    }

    @GET
    @Path("chat")
    @Produces(MediaType.TEXT_PLAIN)
    public String chat(@QueryParam("message") @DefaultValue("What is the current time?") String message) {
        return chatModel.chat(message);
    }

}