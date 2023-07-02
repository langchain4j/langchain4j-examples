package dev.example;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
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

    @Bean
    CustomerSupportAgent customerSupportAgent() {
        return AiServices.builder(CustomerSupportAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withCapacity(20))
                .tools(bookingTools)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(CustomerSupportApplication.class, args);
    }
}
