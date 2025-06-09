package it.dev.langchan4j.example;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class ChatServiceIT {

    private static CountDownLatch countDown;

    @Test
    public void testChat() throws Exception {
        countDown = new CountDownLatch(1);
        URI uri = new URI("ws://localhost:9080/chat");
        ChatClient client = new ChatClient(uri);
        client.sendMessage("When was the LangChain4j launched?");
        countDown.await(120, TimeUnit.SECONDS);
        client.close();
    }

    public static void verify(String message) {
        assertNotNull(message);
        assertTrue(message.contains("2020") || message.contains("2021") ||
            message.contains("2022") || message.contains("2023"),
            message);
        countDown.countDown();
    }

}
