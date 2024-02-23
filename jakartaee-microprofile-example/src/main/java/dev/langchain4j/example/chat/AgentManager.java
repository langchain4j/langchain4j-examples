package dev.langchain4j.example.chat;

import static java.time.Duration.ofSeconds;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

@ApplicationScoped
public class AgentManager {

    private static Logger logger = Logger.getLogger(AgentManager.class.getName());
    
    private static Map<String, ChatAgent> agents = new HashMap<String, ChatAgent>();
    
    private static Map<String,Session> sessions = new HashMap<String,Session>();

    @Inject
    @ConfigProperty(name = "hugging.face.api.key")
    private String HUGGING_FACE_API_KEY;

    @Inject
    @ConfigProperty(name = "chat.model.id")
    private String CHAT_MODEL_ID;

    @Inject
    @ConfigProperty(name = "chat.model.timeout")
    private Integer TIMEOUT;

    @Inject
    @ConfigProperty(name = "chat.model.max.token")
    private Integer MAX_NEW_TOKEN;

    @Inject
    @ConfigProperty(name = "chat.model.temperature")
    private Double TEMPERATURE;

    @Inject
    @ConfigProperty(name = "chat.memory.max.messages")
    private Integer MAX_MESSAGES;

    private ChatAgent createAgent() {
        HuggingFaceChatModel model = HuggingFaceChatModel.builder()
                                         .accessToken(HUGGING_FACE_API_KEY)
                                         .modelId(CHAT_MODEL_ID)
                                         .timeout(ofSeconds(TIMEOUT))
                                         .temperature(TEMPERATURE)
                                         .maxNewTokens(MAX_NEW_TOKEN)
                                         .waitForModel(true)
                                         .build();
        ChatAgent agent = AiServices.builder(ChatAgent.class)
                          .chatLanguageModel(model)
                          .chatMemory(MessageWindowChatMemory.withMaxMessages(MAX_MESSAGES))
                          .build();
        return agent;
    }

    @Counted(name = "createChatAgentCount",
             absolute = true,
             description = "Number of ChatAgent are created.")
    public ChatAgent createAgent(Session session) {
        String sessionId = session.getId();
        logger.info("Creating an agent for session: " + sessionId );
        ChatAgent agent = createAgent();
        agents.put(sessionId, agent);
        sessions.put(sessionId, session);
        return agent;
    }

    public ChatAgent getAgent(String sessionId) {
        return agents.get(sessionId);
    }

    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void remove(String sessionId) {
        agents.remove(sessionId);
        sessions.remove(sessionId);
    }

}
