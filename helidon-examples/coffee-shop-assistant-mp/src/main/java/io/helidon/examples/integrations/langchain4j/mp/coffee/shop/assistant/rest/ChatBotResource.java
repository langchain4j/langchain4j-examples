package io.helidon.examples.integrations.langchain4j.mp.coffee.shop.assistant.rest;

import io.helidon.examples.integrations.langchain4j.mp.coffee.shop.assistant.ai.ChatAiService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.metrics.annotation.Counted;

/**
 * REST resource for interacting with the AI-powered chatbot.
 *
 * This resource provides an endpoint for clients to send chat queries to the AI assistant
 * and receive responses.
 */
@ApplicationScoped
@Path("/")
public class ChatBotResource {

    private final ChatAiService chatAiService;

    /**
     * Constructs a {@code ChatBotResource} instance.
     *
     * @param chatAiService the AI assistant service responsible for handling chat queries
     */
    @Inject
    public ChatBotResource(ChatAiService chatAiService) {
        this.chatAiService = chatAiService;
    }

    /**
     * Handles chat requests by forwarding the user's question to the AI assistant.
     *
     * This endpoint allows clients to send a chat query as a request parameter and receive
     * a text-based response. The request count is tracked for monitoring purposes.
     *
     * @param question the user's chat question (passed as a query parameter)
     * @return the AI assistant's response in plain text
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/chat")
    @Counted
    public String chatWithAssistant(@QueryParam("question") String question) {
        return chatAiService.chat(question);
    }
}
