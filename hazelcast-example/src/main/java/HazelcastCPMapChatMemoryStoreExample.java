import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import dev.langchain4j.community.store.memory.chat.hazelcast.HazelcastCPMapChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Stores chat memory in a strongly-consistent Hazelcast {@code CPMap} (CP Subsystem / Raft).
 * <p>
 * Requires Hazelcast Enterprise (a valid {@code HZ_LICENSEKEY}) and the CP Subsystem enabled.
 * A {@code CPMap} needs a CP quorum of at least 3 members, so this example boots a three-member
 * embedded cluster in-process. The members discover each other via Hazelcast's default multicast,
 * and the code waits for the CP Subsystem to finish discovery before using the store.
 */
public class HazelcastCPMapChatMemoryStoreExample {

    private static final int CP_MEMBER_COUNT = 3;

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.setLicenseKey(System.getenv("HZ_LICENSEKEY"));
        config.getCPSubsystemConfig().setCPMemberCount(CP_MEMBER_COUNT);

        // All three members share the same config; Hazelcast allows reusing one Config instance.
        List<HazelcastInstance> members = new ArrayList<>();
        for (int i = 0; i < CP_MEMBER_COUNT; i++) {
            members.add(Hazelcast.newHazelcastInstance(config));
        }

        HazelcastInstance hz = members.get(0);
        // Block until the CP Subsystem has formed its quorum before using the CPMap.
        hz.getCPSubsystem()
                .getCPSubsystemManagementService()
                .awaitUntilDiscoveryCompleted(60, TimeUnit.SECONDS);

        try {
            ChatMemoryStore store = HazelcastCPMapChatMemoryStore.builder()
                    .hazelcastInstance(hz)
                    .name("chatMemory")
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
            // Terminate (rather than gracefully shut down) to avoid CP membership-change
            // races when all members leave at once.
            members.forEach(member -> member.getLifecycleService().terminate());
        }
    }
}
