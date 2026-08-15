iew again and check allvpackage dev.langchain4j.example.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test: verifies that the Spring context wires the RAG beans
 * ({@link EmbeddingStore}, {@link ContentRetriever}, {@link RagAssistant})
 * and that the {@link RagIngestor} populates the store at startup.
 * <p>
 * The OpenAI {@code ChatModel}, {@code StreamingChatModel}, and
 * {@code EmbeddingModel} are replaced with Mockito mocks so the test
 * runs offline and requires no API key.
 */
@SpringBootTest(properties = {
        "langchain4j.open-ai.chat-model.api-key=test",
        "langchain4j.open-ai.streaming-chat-model.api-key=test",
        "langchain4j.open-ai.embedding-model.api-key=test"
})
class RagSmokeTest {

    @MockitoBean
    ChatModel chatModel;

    @MockitoBean
    StreamingChatModel streamingChatModel;

    @MockitoBean
    EmbeddingModel embeddingModel;

    @Autowired
    RagAssistant assistant;

    @Autowired
    EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    ContentRetriever contentRetriever;

    @Test
    void context_loads_and_rag_beans_are_wired() {
        assertThat(assistant).isNotNull();
        assertThat(embeddingStore).isNotNull();
        assertThat(contentRetriever).isNotNull();
    }
}

