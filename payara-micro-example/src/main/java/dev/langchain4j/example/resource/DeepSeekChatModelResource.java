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

@Path("deepseek")
public class DeepSeekChatModelResource {

    @Inject
    @ConfigProperty(name = "deepseek.api.key")
    private String deepseekApiKey;

    @Inject
    @ConfigProperty(name = "deepseek.chat.model")
    private String modelName;

    private OpenAiChatModel chatModel;

    @PostConstruct
    public void init() {
        chatModel = OpenAiChatModel.builder()
                .apiKey(deepseekApiKey)
                .baseUrl("https://api.deepseek.com")
                .modelName(modelName)
                .temperature(0.1)
                .build();
    }

    @GET
    @Path("chat")
    @Produces(MediaType.TEXT_PLAIN)
    public String chat(@QueryParam("message") @DefaultValue("What can you tell me about reasoning?") String message) {
        return chatModel.chat(message);
    }
}