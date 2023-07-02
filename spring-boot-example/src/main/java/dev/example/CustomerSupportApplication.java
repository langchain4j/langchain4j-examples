package dev.example;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CustomerSupportApplication {

    @Autowired
    ChatLanguageModel chatLanguageModel;

    @Autowired
    BookingTools bookingTools;

    @Autowired
    ModerationModel moderationModel;

    @Bean
    CustomerSupportAgent customerSupportAgent() {
        return AiServices.builder(CustomerSupportAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withCapacity(20))
                .tools(bookingTools)
                .moderationModel(moderationModel)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(CustomerSupportApplication.class, args);
    }
}
