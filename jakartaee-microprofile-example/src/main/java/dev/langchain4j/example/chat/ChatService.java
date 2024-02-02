package dev.langchain4j.example.chat;

import static java.time.Duration.ofSeconds;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint(value = "/chat", encoders = { ChatMessageEncoder.class })
public class ChatService {

    private static Logger logger = Logger.getLogger(ChatService.class.getName());

    private static Map<String, ChatAgent> agents = new HashMap<String, ChatAgent>();
    
    private static Map<String,Session> sessions = new HashMap<String,Session>();

    @Inject
    @ConfigProperty(name = "hugging.face.api.key")
    private String HUGGING_FACE_API_KEY;
    
    @Inject
    @ConfigProperty(name = "chat.model.id")
    private String CHAT_MODEL_ID;

    @Counted(name = "createChatAgentCount",
             absolute = true,
             description = "Number of ChatAgent are created.")
    public ChatAgent createAgent() {
        HuggingFaceChatModel model = HuggingFaceChatModel.builder()
                                         .accessToken(HUGGING_FACE_API_KEY)
                                         .modelId(CHAT_MODEL_ID)
                                         .timeout(ofSeconds(120))
                                         .temperature(1.0)
                                         .maxNewTokens(200)
                                         .waitForModel(true)
                                         .build();
        ChatAgent agent = AiServices.builder(ChatAgent.class)
                          .chatLanguageModel(model)
                          .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                          .build();
        return agent;
    }

    @Timed(name = "chatProcessingTime2",
            absolute = true,
            description = "Time needed chatting to the agent.")
    public String askAgent(Session session, String userMessage) {

        String sessionId = session.getId();
        ChatAgent agent = agents.get(sessionId);
        if (agent == null) {
            logger.info("Creating an agent for session: " + sessionId);
        	agent = createAgent();
            agents.put(sessionId, agent);
            sessions.put(sessionId, session);
        }

        String answer;
        try {
            answer = agent.chat(userMessage).trim();
        } catch (Exception e) {
            answer = "My failure reason is:\n\n" + e.getMessage();
        }

        return answer;

    }
    
    private void sendMessageToSession(Session session, String message) {

        logger.info("Sending message to session: " + session.getId());
        try {
            sessions.get(session.getId())
                    .getBasicRemote()
                    .sendObject(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @OnOpen
    public void onOpen(Session session) {

        logger.info("Server connected to session: " + session.getId());

    }

    @OnMessage
    @Timed(name = "chatProcessingTime",
           absolute = true,
           description = "Time needed chatting to the agent.")
    public void onMessage(String message, Session session) {

        logger.info("Server received message \"" + message + "\" "
                    + "from session: " + session.getId());

        String answer = askAgent(session, message);
        sendMessageToSession(session, answer);

    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {

        String sessionId = session.getId();
        logger.info("Session " + sessionId
                    + " was closed with reason " + closeReason.getCloseCode());
        agents.remove(sessionId);
        sessions.remove(sessionId);

    }

    @OnError
    public void onError(Session session, Throwable throwable) {

        logger.info("WebSocket error for " + session.getId() + " "
                    + throwable.getMessage());

    }

}
