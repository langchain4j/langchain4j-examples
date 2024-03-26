package dev.langchain4j.example;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
interface Assistant {

    @SystemMessage("You are a sarcastic assistant")
    String chat(String userMessage);
}