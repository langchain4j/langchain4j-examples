package dev.langchain4j.example.chat;

import java.util.logging.Logger;

import org.eclipse.microprofile.metrics.annotation.Timed;

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

    @Inject
    AgentManager manager;

    private void sendMessageToSession(Session session, String message) {

        logger.info("Sending message to session: " + session.getId());
        try {
            manager.getSession(session.getId())
                   .getBasicRemote()
                   .sendObject(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String askAgent(Session session, String userMessage) {

        String answer;
        try {
            ChatAgent agent = manager.getAgent(session.getId());
            if (agent == null) {
                agent = manager.createAgent(session);
            }
            answer = agent.chat(userMessage).trim();
        } catch (Exception e) {
            answer = "My failure reason is:\n\n" + e.getMessage();
        }

        return answer;

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
        manager.remove(sessionId);

    }

    @OnError
    public void onError(Session session, Throwable throwable) {

        logger.info("WebSocket error for " + session.getId() + " "
                    + throwable.getMessage());

    }

}
