import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import dev.langchain4j.community.store.memory.chat.hazelcast.HazelcastChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import java.util.List;

/**
 * Stores chat memory in a Hazelcast {@code IMap} using the open-source
 * {@code langchain4j-community-hazelcast} module. Runs against the open-source
 * Community Edition with no license.
 */
public class HazelcastChatMemoryStoreExample {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(new Config());
        try {
            ChatMemoryStore store = HazelcastChatMemoryStore.builder()
                    .hazelcastInstance(hz)
                    .name("chatMemory") // optional, defaults to "chatMemory"
                    .build();

            String memoryId = "user-1";
            store.updateMessages(
                    memoryId,
                    List.of(
                            UserMessage.from("Hello, my name is Andrii."),
                            AiMessage.from("Hi Andrii, how can I help you today?")));

            List<ChatMessage> messages = store.getMessages(memoryId);
            messages.forEach(System.out::println);
        } finally {
            hz.shutdown();
        }
    }
}
