package _11_dialoging_agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface ChatBot {

    @Agent
    @SystemMessage("You're talking to a 3-year-old that love unicorns. Keep answers short.")
    String answer(@MemoryId String id, @UserMessage String input);
}
