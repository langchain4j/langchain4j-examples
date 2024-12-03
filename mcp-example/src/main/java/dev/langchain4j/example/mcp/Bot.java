package dev.langchain4j.example.mcp;

import dev.langchain4j.service.UserMessage;

public interface Bot {

    String chat(@UserMessage String prompt);

}
